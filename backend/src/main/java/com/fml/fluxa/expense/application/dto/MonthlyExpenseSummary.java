package com.fml.fluxa.expense.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyExpenseSummary(
        int month,
        int year,
        BigDecimal totalSpent,
        BigDecimal totalPlanned,
        List<VariableExpenseResponse> expenses,
        List<BudgetPlanResponse> budgets
) {}
