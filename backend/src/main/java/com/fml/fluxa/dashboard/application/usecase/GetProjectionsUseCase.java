package com.fml.fluxa.dashboard.application.usecase;

import com.fml.fluxa.commitment.domain.model.CommitmentFrequency;
import com.fml.fluxa.commitment.infrastructure.persistence.FixedCommitmentJpaRepository;
import com.fml.fluxa.credit.domain.model.CreditStatus;
import com.fml.fluxa.credit.domain.model.CreditType;
import com.fml.fluxa.credit.infrastructure.persistence.CreditCardJpaRepository;
import com.fml.fluxa.credit.infrastructure.persistence.CreditJpaRepository;
import com.fml.fluxa.dashboard.application.dto.MonthlyProjectionDto;
import com.fml.fluxa.dashboard.application.dto.ProjectionsResponse;
import com.fml.fluxa.expense.infrastructure.persistence.VariableExpenseJpaRepository;
import com.fml.fluxa.income.domain.model.IncomeFrequency;
import com.fml.fluxa.income.domain.model.IncomeType;
import com.fml.fluxa.income.infrastructure.persistence.IncomeSourceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GetProjectionsUseCase {

    private final IncomeSourceJpaRepository incomeSourceRepo;
    private final FixedCommitmentJpaRepository commitmentRepo;
    private final VariableExpenseJpaRepository expenseRepo;
    private final CreditJpaRepository creditRepo;
    private final CreditCardJpaRepository creditCardRepo;

    public GetProjectionsUseCase(IncomeSourceJpaRepository incomeSourceRepo,
                                 FixedCommitmentJpaRepository commitmentRepo,
                                 VariableExpenseJpaRepository expenseRepo,
                                 CreditJpaRepository creditRepo,
                                 CreditCardJpaRepository creditCardRepo) {
        this.incomeSourceRepo = incomeSourceRepo;
        this.commitmentRepo = commitmentRepo;
        this.expenseRepo = expenseRepo;
        this.creditRepo = creditRepo;
        this.creditCardRepo = creditCardRepo;
    }

    @Transactional(readOnly = true)
    public ProjectionsResponse getProjections(Long userId, int months) {
        // Ingreso mensual proyectado desde fuentes fijas activas
        BigDecimal projectedIncome = calcProjectedMonthlyIncome(userId);

        // Compromisos fijos mensuales activos
        BigDecimal projectedCommitments = calcProjectedCommitments(userId);

        // AVG gastos variables últimos 3 meses
        BigDecimal avgVariableExpenses = calcAvgVariableExpenses(userId);

        // Obligaciones crédito: cuotas + mínimos tarjetas
        BigDecimal creditObligations = calcCreditObligations(userId);

        List<MonthlyProjectionDto> projections = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 1; i <= months; i++) {
            YearMonth ym = current.plusMonths(i);
            BigDecimal netFlow = projectedIncome
                    .subtract(projectedCommitments)
                    .subtract(avgVariableExpenses)
                    .subtract(creditObligations);

            String label = ym.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es"))
                    + " " + ym.getYear();

            projections.add(new MonthlyProjectionDto(
                    ym.getMonthValue(), ym.getYear(), label,
                    projectedIncome, projectedCommitments,
                    avgVariableExpenses, creditObligations, netFlow
            ));
        }

        BigDecimal totalIncome = projections.stream()
                .map(MonthlyProjectionDto::projectedIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = projections.stream()
                .map(p -> p.projectedCommitments()
                        .add(p.projectedVariableExpenses())
                        .add(p.projectedCreditObligations()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = projections.stream()
                .map(MonthlyProjectionDto::projectedNetFlow)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal n = BigDecimal.valueOf(months);
        return new ProjectionsResponse(
                months, projections,
                totalIncome.divide(n, 2, RoundingMode.HALF_UP),
                totalExpenses.divide(n, 2, RoundingMode.HALF_UP),
                totalNet.divide(n, 2, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calcProjectedMonthlyIncome(Long userId) {
        return incomeSourceRepo
                .findByUserIdAndTypeAndIsActiveTrueAndDeletedAtIsNull(userId, IncomeType.FIXED)
                .stream()
                .map(s -> {
                    if (s.getFrequency() == IncomeFrequency.BIWEEKLY) {
                        return s.getExpectedAmount().multiply(new BigDecimal("2"));
                    } else if (s.getFrequency() == IncomeFrequency.WEEKLY) {
                        return s.getExpectedAmount().multiply(new BigDecimal("4"));
                    } else if (s.getFrequency() == IncomeFrequency.ONE_TIME) {
                        return BigDecimal.ZERO;
                    }
                    return s.getExpectedAmount();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcProjectedCommitments(Long userId) {
        return commitmentRepo
                .findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId)
                .stream()
                .map(c -> {
                    // Para frecuencias no mensuales, prorratear al mes
                    if (c.getFrequency() == CommitmentFrequency.BIMONTHLY) {
                        return c.getEstimatedAmount().divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                    } else if (c.getFrequency() == CommitmentFrequency.QUARTERLY) {
                        return c.getEstimatedAmount().divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
                    } else if (c.getFrequency() == CommitmentFrequency.ANNUAL) {
                        return c.getEstimatedAmount().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
                    }
                    return c.getEstimatedAmount();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcAvgVariableExpenses(Long userId) {
        LocalDate today = LocalDate.now();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int i = 1; i <= 3; i++) {
            LocalDate ref = today.minusMonths(i);
            BigDecimal spent = expenseRepo.sumByUserAndPeriod(userId, ref.getMonthValue(), ref.getYear());
            if (spent != null && spent.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(spent);
                count++;
            }
        }
        if (count == 0) return BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcCreditObligations(Long userId) {
        // Cuotas mensuales de créditos activos (no tarjeta)
        BigDecimal installments = creditRepo
                .findByUserIdAndStatusAndDeletedAtIsNull(userId, CreditStatus.ACTIVE)
                .stream()
                .filter(c -> c.getType() != CreditType.CREDIT_CARD)
                .filter(c -> c.getMonthlyInstallment() != null)
                .map(c -> c.getMonthlyInstallment())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pago mínimo de tarjetas activas
        BigDecimal minPayments = creditCardRepo.sumMinimumPaymentsByUser(userId);

        return installments.add(minPayments);
    }
}
