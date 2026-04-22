package com.fml.fluxa.expense.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VariableExpenseRequest(
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate expenseDate,
        @Size(max = 500) String description,
        List<String> tags,
        @Size(max = 500) String receiptUrl
) {}
