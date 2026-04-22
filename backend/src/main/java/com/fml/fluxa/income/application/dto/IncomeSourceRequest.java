package com.fml.fluxa.income.application.dto;

import com.fml.fluxa.income.domain.model.IncomeFrequency;
import com.fml.fluxa.income.domain.model.IncomeType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeSourceRequest(
        @NotBlank @Size(min = 2, max = 150)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        IncomeType type,

        @NotNull @DecimalMin("0.00")
        BigDecimal expectedAmount,

        @NotNull
        IncomeFrequency frequency,

        @NotNull
        LocalDate startDate,

        LocalDate endDate,

        Long categoryId
) {}
