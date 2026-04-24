package com.fml.fluxa.dashboard.application.usecase;

import com.fml.fluxa.commitment.domain.model.CommitmentStatus;
import com.fml.fluxa.commitment.infrastructure.persistence.CommitmentRecordJpaRepository;
import com.fml.fluxa.commitment.infrastructure.persistence.FixedCommitmentJpaRepository;
import com.fml.fluxa.dashboard.application.dto.DashboardSummaryResponse;
import com.fml.fluxa.dashboard.application.dto.DashboardSummaryResponse.HealthStatus;
import com.fml.fluxa.dashboard.application.dto.TopExpenseDto;
import com.fml.fluxa.dashboard.application.dto.UpcomingPaymentDto;
import com.fml.fluxa.credit.infrastructure.persistence.CreditCardJpaRepository;
import com.fml.fluxa.credit.infrastructure.persistence.CreditJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.BudgetPlanJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.ExpenseCategoryJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.VariableExpenseJpaRepository;
import com.fml.fluxa.income.infrastructure.persistence.IncomeRecordJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GetDashboardSummaryUseCase {

    private final IncomeRecordJpaRepository incomeRecordRepo;
    private final CommitmentRecordJpaRepository commitmentRecordRepo;
    private final FixedCommitmentJpaRepository commitmentRepo;
    private final VariableExpenseJpaRepository expenseRepo;
    private final BudgetPlanJpaRepository budgetRepo;
    private final ExpenseCategoryJpaRepository categoryRepo;
    private final CreditJpaRepository creditRepo;
    private final CreditCardJpaRepository creditCardRepo;

    public GetDashboardSummaryUseCase(
            IncomeRecordJpaRepository incomeRecordRepo,
            CommitmentRecordJpaRepository commitmentRecordRepo,
            FixedCommitmentJpaRepository commitmentRepo,
            VariableExpenseJpaRepository expenseRepo,
            BudgetPlanJpaRepository budgetRepo,
            ExpenseCategoryJpaRepository categoryRepo,
            CreditJpaRepository creditRepo,
            CreditCardJpaRepository creditCardRepo) {
        this.incomeRecordRepo = incomeRecordRepo;
        this.commitmentRecordRepo = commitmentRecordRepo;
        this.commitmentRepo = commitmentRepo;
        this.expenseRepo = expenseRepo;
        this.budgetRepo = budgetRepo;
        this.categoryRepo = categoryRepo;
        this.creditRepo = creditRepo;
        this.creditCardRepo = creditCardRepo;
    }

    // Evita NullPointerException cuando JPA retorna null en consultas SUM sin filas
    private static BigDecimal safe(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long userId, int month, int year) {
        // Ingresos
        BigDecimal totalIncome = safe(incomeRecordRepo.sumReceivedByPeriod(userId, month, year));
        BigDecimal totalIncomeExpected = safe(incomeRecordRepo.sumExpectedByPeriod(userId, month, year));

        // Compromisos
        var commitmentRecords = commitmentRecordRepo.findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year);
        BigDecimal totalCommitments = safe(commitmentRecordRepo.sumEstimatedByPeriod(userId, month, year));
        BigDecimal totalCommitmentsPaid = safe(commitmentRecordRepo.sumPaidByPeriod(userId, month, year));
        long pendingCount = commitmentRecords.stream()
                .filter(r -> r.getStatus() == CommitmentStatus.PENDING).count();
        long overdueCount = commitmentRecords.stream()
                .filter(r -> r.getStatus() == CommitmentStatus.OVERDUE).count();

        // Gastos variables
        BigDecimal totalExpenses = safe(expenseRepo.sumByUserAndPeriod(userId, month, year));
        BigDecimal totalPlanned = safe(budgetRepo.sumPlannedByPeriod(userId, month, year));

        // Obligaciones de crédito del mes (cuotas + mínimos tarjetas)
        BigDecimal totalCreditObligations = safe(creditRepo.sumMonthlyInstallmentsByUser(userId))
                .add(safe(creditCardRepo.sumMinimumPaymentsByUser(userId)));

        // Flujo neto (incluye créditos)
        BigDecimal netFlow = totalIncome.subtract(totalCommitments)
                .subtract(totalExpenses).subtract(totalCreditObligations);

        // Indicador de salud financiera (incluye créditos en obligaciones)
        BigDecimal commitmentRatio = BigDecimal.ZERO;
        HealthStatus healthStatus = HealthStatus.GREEN;
        if (totalIncomeExpected.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal obligations = totalCommitments.add(totalExpenses).add(totalCreditObligations);
            commitmentRatio = obligations
                    .multiply(new BigDecimal("100"))
                    .divide(totalIncomeExpected, 2, RoundingMode.HALF_UP);
            if (commitmentRatio.compareTo(new BigDecimal("60")) > 0) {
                healthStatus = HealthStatus.RED;
            } else if (commitmentRatio.compareTo(new BigDecimal("40")) > 0) {
                healthStatus = HealthStatus.YELLOW;
            }
        }

        // Próximos vencimientos (próximos 7 días)
        LocalDate today = LocalDate.now();
        Map<Long, String> commitmentNames = commitmentRepo.findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId)
                .stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        List<UpcomingPaymentDto> upcoming = commitmentRecordRepo
                .findUpcomingPending(userId, today, today.plusDays(7))
                .stream()
                .map(r -> new UpcomingPaymentDto(
                        r.getCommitmentId(),
                        commitmentNames.getOrDefault(r.getCommitmentId(), "Sin nombre"),
                        r.getEstimatedAmount(),
                        r.getDueDate(),
                        (int) ChronoUnit.DAYS.between(today, r.getDueDate())
                ))
                .toList();

        // Top 5 categorías de gasto
        Map<Long, String> catNames = categoryRepo.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        List<TopExpenseDto> topExpenses = expenseRepo.findTop5CategoriesByPeriod(userId, month, year)
                .stream()
                .map(p -> {
                    double pct = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                            ? p.getTotal().multiply(new BigDecimal("100"))
                                    .divide(totalExpenses, 2, RoundingMode.HALF_UP)
                                    .doubleValue()
                            : 0.0;
                    return new TopExpenseDto(
                            p.getCategoryId(),
                            catNames.getOrDefault(p.getCategoryId(), "Sin categoría"),
                            p.getTotal(),
                            pct);
                })
                .toList();

        return new DashboardSummaryResponse(
                month, year,
                totalIncome, totalIncomeExpected,
                totalCommitments, totalCommitmentsPaid,
                (int) pendingCount, (int) overdueCount,
                totalExpenses, totalPlanned,
                netFlow,
                commitmentRatio, healthStatus,
                upcoming, topExpenses
        );
    }
}
