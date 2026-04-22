package com.fml.fluxa.expense.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetPlanRequest(
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.00") BigDecimal plannedAmount,
        @NotNull @Min(1) @Max(12) int month,
        @NotNull @Min(2000) int year
) {}
