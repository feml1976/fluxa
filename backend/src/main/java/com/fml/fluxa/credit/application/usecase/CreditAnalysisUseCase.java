package com.fml.fluxa.credit.application.usecase;

import com.fml.fluxa.credit.application.dto.*;
import com.fml.fluxa.credit.domain.model.Credit;
import com.fml.fluxa.credit.domain.model.CreditCard;
import com.fml.fluxa.credit.domain.model.CreditType;
import com.fml.fluxa.credit.infrastructure.persistence.CreditCardJpaRepository;
import com.fml.fluxa.credit.infrastructure.persistence.CreditJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreditAnalysisUseCase {

    private final CreditJpaRepository creditRepo;
    private final CreditCardJpaRepository cardRepo;

    public CreditAnalysisUseCase(CreditJpaRepository creditRepo,
                                 CreditCardJpaRepository cardRepo) {
        this.creditRepo = creditRepo;
        this.cardRepo = cardRepo;
    }

    @Transactional(readOnly = true)
    public CreditAnalysisResponse analyze(Long userId, Long creditId) {
        Credit credit = creditRepo.findByIdAndUserIdAndDeletedAtIsNull(creditId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Crédito no encontrado"));

        if (credit.getType() == CreditType.CREDIT_CARD) {
            CreditCard card = cardRepo.findByCreditId(creditId)
                    .orElseThrow(() -> new ResourceNotFoundException("Datos de tarjeta no encontrados"));
            return analyzeCreditCard(credit, card);
        }
        return analyzeLoan(credit);
    }

    // ── Análisis de tarjeta de crédito ───────────────────────────

    private CreditAnalysisResponse analyzeCreditCard(Credit credit, CreditCard card) {
        List<String> alerts = new ArrayList<>();
        CreditAlertLevel level = CreditAlertLevel.GREEN;

        BigDecimal used = card.getCreditLimitPurchases().subtract(card.getAvailablePurchases());
        BigDecimal pct = card.getCreditLimitPurchases().compareTo(BigDecimal.ZERO) > 0
                ? used.multiply(new BigDecimal("100"))
                        .divide(card.getCreditLimitPurchases(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        if (card.getLateInterest().compareTo(BigDecimal.ZERO) > 0) {
            level = CreditAlertLevel.RED;
            alerts.add("ALERTA CRÍTICA: tiene intereses de mora por " +
                       card.getLateInterest().toPlainString());
        }
        if (card.getAvailablePurchases().compareTo(BigDecimal.ZERO) <= 0) {
            level = CreditAlertLevel.RED;
            alerts.add("Cupo disponible agotado");
        } else if (pct.compareTo(new BigDecimal("80")) >= 0 && level != CreditAlertLevel.RED) {
            level = CreditAlertLevel.YELLOW;
            alerts.add("Utilización alta: " + pct.toPlainString() + "%");
        }
        if (card.getAlternateMinimumPayment().compareTo(BigDecimal.ZERO) > 0) {
            alerts.add("ADVERTENCIA: el Pago Mínimo Alterno puede extender su deuda hasta 36 cuotas adicionales");
        }

        // Proyección con pago mínimo (simulación mes a mes)
        BigDecimal mv = credit.getInterestRateMv().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal balance = used;
        BigDecimal minPay = card.getMinimumPayment();
        BigDecimal totalInterest = BigDecimal.ZERO;
        int months = 0;

        if (minPay.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
            while (balance.compareTo(BigDecimal.ZERO) > 0 && months < 600) {
                BigDecimal interest = balance.multiply(mv).setScale(2, RoundingMode.HALF_UP);
                totalInterest = totalInterest.add(interest);
                balance = balance.add(interest).subtract(minPay);
                if (balance.compareTo(BigDecimal.ZERO) < 0) balance = BigDecimal.ZERO;
                months++;
                // Si la cuota mínima no cubre ni los intereses, el balance nunca baja
                if (interest.compareTo(minPay) >= 0) {
                    months = -1;
                    break;
                }
            }
        }

        return new CreditAnalysisResponse(
                credit.getId(),
                level,
                alerts,
                pct,
                months < 0 ? null : months,
                months < 0 ? null : totalInterest,
                card.getAlternateMinimumPayment().compareTo(BigDecimal.ZERO) > 0,
                null, null, null, null
        );
    }

    // ── Análisis de crédito (personal / hipoteca / vehículo) ──────

    private CreditAnalysisResponse analyzeLoan(Credit credit) {
        List<String> alerts = new ArrayList<>();
        CreditAlertLevel level = CreditAlertLevel.GREEN;

        BigDecimal mv = credit.getInterestRateMv().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal balance = credit.getCurrentBalance();
        BigDecimal installment = credit.getMonthlyInstallment();

        if (installment == null || installment.compareTo(BigDecimal.ZERO) <= 0
                || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return new CreditAnalysisResponse(
                    credit.getId(), level, alerts,
                    null, null, null, false,
                    null, null, BigDecimal.ZERO, List.of()
            );
        }

        // Cuotas restantes (aproximación geométrica)
        int remaining = estimateRemainingInstallments(balance, mv, installment);
        LocalDate projectedPayoff = LocalDate.now().plusMonths(remaining);

        // Tabla de amortización (máx 360 filas para no sobrecargar la respuesta)
        List<AmortizationRow> table = buildAmortizationTable(balance, mv, installment,
                Math.min(remaining, 360));

        BigDecimal totalInterest = table.stream()
                .map(AmortizationRow::interestPortion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (mv.multiply(new BigDecimal("100")).compareTo(new BigDecimal("2")) > 0) {
            level = CreditAlertLevel.YELLOW;
            alerts.add("Tasa MV superior al 2% mensual");
        }
        if (remaining > 120) {
            alerts.add("Más de 10 años de plazo restante");
        }

        return new CreditAnalysisResponse(
                credit.getId(), level, alerts,
                null, null, null, false,
                remaining, projectedPayoff, totalInterest, table
        );
    }

    private int estimateRemainingInstallments(BigDecimal balance, BigDecimal mv, BigDecimal installment) {
        if (mv.compareTo(BigDecimal.ZERO) == 0) {
            return balance.divide(installment, 0, RoundingMode.CEILING).intValue();
        }
        // n = -ln(1 - i*PV/PMT) / ln(1+i)
        double i = mv.doubleValue();
        double pv = balance.doubleValue();
        double pmt = installment.doubleValue();
        if (pmt <= i * pv) return 9999;
        double n = -Math.log(1 - i * pv / pmt) / Math.log(1 + i);
        return (int) Math.ceil(n);
    }

    private List<AmortizationRow> buildAmortizationTable(BigDecimal balance, BigDecimal mv,
                                                          BigDecimal installment, int rows) {
        List<AmortizationRow> table = new ArrayList<>();
        BigDecimal remaining = balance;
        for (int i = 1; i <= rows && remaining.compareTo(BigDecimal.ZERO) > 0; i++) {
            BigDecimal interest = remaining.multiply(mv).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capital = installment.subtract(interest);
            if (capital.compareTo(remaining) > 0) capital = remaining;
            remaining = remaining.subtract(capital).max(BigDecimal.ZERO);
            table.add(new AmortizationRow(i, installment, interest, capital, remaining));
        }
        return table;
    }

    // ── Resolución de nivel de alerta (para lista) ───────────────

    CreditAlertLevel resolveAlertLevel(Credit credit, CreditCard card) {
        if (credit.getType() != CreditType.CREDIT_CARD || card == null) {
            BigDecimal mv = credit.getInterestRateMv();
            if (mv != null && mv.compareTo(new BigDecimal("2")) > 0) return CreditAlertLevel.YELLOW;
            return CreditAlertLevel.GREEN;
        }
        if (card.getLateInterest().compareTo(BigDecimal.ZERO) > 0
                || card.getAvailablePurchases().compareTo(BigDecimal.ZERO) <= 0) {
            return CreditAlertLevel.RED;
        }
        BigDecimal used = card.getCreditLimitPurchases().subtract(card.getAvailablePurchases());
        if (card.getCreditLimitPurchases().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pct = used.multiply(new BigDecimal("100"))
                    .divide(card.getCreditLimitPurchases(), 2, RoundingMode.HALF_UP);
            if (pct.compareTo(new BigDecimal("80")) >= 0) return CreditAlertLevel.YELLOW;
        }
        return CreditAlertLevel.GREEN;
    }
}
