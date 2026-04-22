package com.fml.fluxa.expense.application.usecase;

import com.fml.fluxa.expense.application.dto.BudgetPlanResponse;
import com.fml.fluxa.expense.application.dto.MonthlyExpenseSummary;
import com.fml.fluxa.expense.application.dto.VariableExpenseResponse;
import com.fml.fluxa.expense.infrastructure.persistence.BudgetPlanJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.ExpenseCategoryJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.VariableExpenseJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonthlyExpenseUseCase {

    private final VariableExpenseJpaRepository expenseRepo;
    private final BudgetPlanJpaRepository budgetRepo;
    private final ExpenseCategoryJpaRepository categoryRepo;

    public MonthlyExpenseUseCase(VariableExpenseJpaRepository expenseRepo,
                                 BudgetPlanJpaRepository budgetRepo,
                                 ExpenseCategoryJpaRepository categoryRepo) {
        this.expenseRepo = expenseRepo;
        this.budgetRepo = budgetRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional(readOnly = true)
    public MonthlyExpenseSummary getSummary(Long userId, int month, int year) {
        Map<Long, String> catNames = buildCategoryNameMap(userId);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<VariableExpenseResponse> expenses = expenseRepo
                .findByUserIdAndExpenseDateBetweenAndDeletedAtIsNullOrderByExpenseDateDesc(userId, start, end)
                .stream()
                .map(e -> VariableExpenseResponse.from(e, catNames.getOrDefault(e.getCategoryId(), "Sin categoría")))
                .toList();

        List<BudgetPlanResponse> budgets = budgetRepo
                .findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year)
                .stream()
                .map(b -> {
                    BigDecimal spent = expenseRepo.sumByUserCategoryAndPeriod(userId, b.getCategoryId(), month, year);
                    return BudgetPlanResponse.from(b, catNames.getOrDefault(b.getCategoryId(), "Sin categoría"), spent);
                })
                .toList();

        BigDecimal totalSpent = expenseRepo.sumByUserAndPeriod(userId, month, year);
        BigDecimal totalPlanned = budgetRepo.sumPlannedByPeriod(userId, month, year);

        return new MonthlyExpenseSummary(month, year, totalSpent, totalPlanned, expenses, budgets);
    }

    private Map<Long, String> buildCategoryNameMap(Long userId) {
        return categoryRepo.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));
    }
}
