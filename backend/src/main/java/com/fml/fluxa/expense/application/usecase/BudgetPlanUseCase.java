package com.fml.fluxa.expense.application.usecase;

import com.fml.fluxa.expense.application.dto.BudgetPlanRequest;
import com.fml.fluxa.expense.application.dto.BudgetPlanResponse;
import com.fml.fluxa.expense.domain.model.BudgetPlan;
import com.fml.fluxa.expense.infrastructure.persistence.BudgetPlanJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.ExpenseCategoryJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.VariableExpenseJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetPlanUseCase {

    private final BudgetPlanJpaRepository budgetRepo;
    private final VariableExpenseJpaRepository expenseRepo;
    private final ExpenseCategoryJpaRepository categoryRepo;

    public BudgetPlanUseCase(BudgetPlanJpaRepository budgetRepo,
                             VariableExpenseJpaRepository expenseRepo,
                             ExpenseCategoryJpaRepository categoryRepo) {
        this.budgetRepo = budgetRepo;
        this.expenseRepo = expenseRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional
    public BudgetPlanResponse saveOrUpdate(Long userId, BudgetPlanRequest req) {
        BudgetPlan plan = budgetRepo
                .findByUserIdAndCategoryIdAndPeriodMonthAndPeriodYear(
                        userId, req.categoryId(), req.month(), req.year())
                .orElseGet(() -> BudgetPlan.builder()
                        .userId(userId)
                        .categoryId(req.categoryId())
                        .periodMonth(req.month())
                        .periodYear(req.year())
                        .build());

        plan.setPlannedAmount(req.plannedAmount());
        plan.setSuggestedAmount(calculateSuggestion(userId, req.categoryId(), req.month(), req.year()));

        BudgetPlan saved = budgetRepo.save(plan);
        BigDecimal spent = expenseRepo.sumByUserCategoryAndPeriod(userId, req.categoryId(), req.month(), req.year());
        String catName = resolveCategoryName(userId, req.categoryId());
        return BudgetPlanResponse.from(saved, catName, spent);
    }

    @Transactional(readOnly = true)
    public List<BudgetPlanResponse> listByPeriod(Long userId, int month, int year) {
        Map<Long, String> catNames = buildCategoryNameMap(userId);
        return budgetRepo.findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year)
                .stream()
                .map(b -> {
                    BigDecimal spent = expenseRepo.sumByUserCategoryAndPeriod(userId, b.getCategoryId(), month, year);
                    return BudgetPlanResponse.from(b, catNames.getOrDefault(b.getCategoryId(), "Sin categoría"), spent);
                })
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long id) {
        BudgetPlan plan = budgetRepo.findById(id)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado"));
        budgetRepo.delete(plan);
    }

    /** AVG últimos 3 meses para la categoría, redondeado al alza al múltiplo de $10.000 más cercano */
    private BigDecimal calculateSuggestion(Long userId, Long categoryId, int month, int year) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int i = 1; i <= 3; i++) {
            int m = month - i;
            int y = year;
            if (m <= 0) { m += 12; y--; }
            BigDecimal spent = expenseRepo.sumByUserCategoryAndPeriod(userId, categoryId, m, y);
            if (spent != null && spent.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(spent);
                count++;
            }
        }
        if (count == 0) return null;
        BigDecimal avg = total.divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP);
        // Redondear al alza al múltiplo de 10.000
        BigDecimal ten = new BigDecimal("10000");
        return avg.divide(ten, 0, RoundingMode.CEILING).multiply(ten);
    }

    private String resolveCategoryName(Long userId, Long categoryId) {
        return categoryRepo.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .map(c -> c.getName())
                .orElse("Sin categoría");
    }

    private Map<Long, String> buildCategoryNameMap(Long userId) {
        return categoryRepo.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));
    }
}
