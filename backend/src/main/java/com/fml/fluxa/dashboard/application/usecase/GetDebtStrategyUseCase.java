package com.fml.fluxa.dashboard.application.usecase;

import com.fml.fluxa.credit.domain.model.Credit;
import com.fml.fluxa.credit.domain.model.CreditStatus;
import com.fml.fluxa.credit.domain.model.CreditType;
import com.fml.fluxa.credit.infrastructure.persistence.CreditCardJpaRepository;
import com.fml.fluxa.credit.infrastructure.persistence.CreditJpaRepository;
import com.fml.fluxa.dashboard.application.dto.DebtStrategyItem;
import com.fml.fluxa.dashboard.application.dto.DebtStrategyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GetDebtStrategyUseCase {

    private final CreditJpaRepository creditRepo;
    private final CreditCardJpaRepository cardRepo;

    public GetDebtStrategyUseCase(CreditJpaRepository creditRepo,
                                  CreditCardJpaRepository cardRepo) {
        this.creditRepo = creditRepo;
        this.cardRepo = cardRepo;
    }

    @Transactional(readOnly = true)
    public List<DebtStrategyResponse> getStrategies(Long userId) {
        List<Credit> active = creditRepo.findByUserIdAndStatusAndDeletedAtIsNull(userId, CreditStatus.ACTIVE);

        List<StrategyDebt> debts = new ArrayList<>();
        for (Credit c : active) {
            BigDecimal balance;
            BigDecimal payment;
            if (c.getType() == CreditType.CREDIT_CARD) {
                var card = cardRepo.findByCreditId(c.getId()).orElse(null);
                balance = card != null
                        ? card.getCreditLimitPurchases().subtract(card.getAvailablePurchases())
                        : c.getCurrentBalance();
                payment = card != null ? card.getMinimumPayment() : BigDecimal.ZERO;
            } else {
                balance = c.getCurrentBalance();
                payment = c.getMonthlyInstallment() != null ? c.getMonthlyInstallment() : BigDecimal.ZERO;
            }
            if (balance.compareTo(BigDecimal.ZERO) > 0 && payment.compareTo(BigDecimal.ZERO) > 0) {
                debts.add(new StrategyDebt(c.getId(), c.getName(),
                        c.getType().name(), balance, c.getInterestRateMv(), payment));
            }
        }

        if (debts.isEmpty()) return List.of();

        BigDecimal totalDebt = debts.stream().map(StrategyDebt::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal baselineInterest = debts.stream()
                .map(d -> simulateTotalInterest(d.balance(), d.mv(), d.payment()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StrategyDebt> avalancheOrder = debts.stream()
                .sorted(Comparator.comparing(StrategyDebt::mv).reversed())
                .toList();

        List<StrategyDebt> snowballOrder = debts.stream()
                .sorted(Comparator.comparing(StrategyDebt::balance))
                .toList();

        return List.of(
                buildStrategy("Avalanche",
                        "Paga primero el crédito con mayor tasa de interés. Minimiza el costo financiero total.",
                        avalancheOrder, totalDebt, baselineInterest),
                buildStrategy("Snowball",
                        "Paga primero el crédito con menor saldo. Genera motivación al liquidar deudas rápido.",
                        snowballOrder, totalDebt, baselineInterest)
        );
    }

    private DebtStrategyResponse buildStrategy(String name, String description,
                                                List<StrategyDebt> ordered,
                                                BigDecimal totalDebt, BigDecimal baselineInterest) {
        List<DebtStrategyItem> items = new ArrayList<>();
        BigDecimal totalInterest = BigDecimal.ZERO;
        int maxMonths = 0;

        for (int i = 0; i < ordered.size(); i++) {
            StrategyDebt d = ordered.get(i);
            int months = estimateMonths(d.balance(), d.mv(), d.payment());
            BigDecimal interest = simulateTotalInterest(d.balance(), d.mv(), d.payment());
            totalInterest = totalInterest.add(interest);
            maxMonths = Math.max(maxMonths, months == 9999 ? 0 : months);
            items.add(new DebtStrategyItem(
                    i + 1, d.creditId(), d.name(), d.type(),
                    d.balance(), d.mv(), d.payment(),
                    months == 9999 ? -1 : months, interest
            ));
        }

        BigDecimal saved = baselineInterest.subtract(totalInterest).max(BigDecimal.ZERO);
        return new DebtStrategyResponse(name, description, items,
                totalDebt, totalInterest, maxMonths, saved);
    }

    private BigDecimal simulateTotalInterest(BigDecimal balance, BigDecimal mv, BigDecimal payment) {
        BigDecimal rate = mv.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal remaining = balance;
        BigDecimal totalInterest = BigDecimal.ZERO;
        int max = 600;
        while (remaining.compareTo(BigDecimal.ZERO) > 0 && max-- > 0) {
            BigDecimal interest = remaining.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            if (interest.compareTo(payment) >= 0) break;
            totalInterest = totalInterest.add(interest);
            remaining = remaining.add(interest).subtract(payment);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        }
        return totalInterest;
    }

    private int estimateMonths(BigDecimal balance, BigDecimal mv, BigDecimal payment) {
        BigDecimal rate = mv.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return balance.divide(payment, 0, RoundingMode.CEILING).intValue();
        }
        double i = rate.doubleValue();
        double pv = balance.doubleValue();
        double pmt = payment.doubleValue();
        if (pmt <= i * pv) return 9999;
        return (int) Math.ceil(-Math.log(1 - i * pv / pmt) / Math.log(1 + i));
    }

    private record StrategyDebt(Long creditId, String name, String type,
                                 BigDecimal balance, BigDecimal mv, BigDecimal payment) {}
}
