package com.fml.fluxa.expense.application.dto;

import com.fml.fluxa.expense.domain.model.BudgetPlan;
import java.math.BigDecimal;

public record BudgetPlanResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal plannedAmount,
        BigDecimal suggestedAmount,
        BigDecimal spentAmount,
        int periodMonth,
        int periodYear
) {
    public static BudgetPlanResponse from(BudgetPlan b, String categoryName, BigDecimal spent) {
        return new BudgetPlanResponse(
                b.getId(),
                b.getCategoryId(),
                categoryName,
                b.getPlannedAmount(),
                b.getSuggestedAmount(),
                spent,
                b.getPeriodMonth(),
                b.getPeriodYear()
        );
    }
}
