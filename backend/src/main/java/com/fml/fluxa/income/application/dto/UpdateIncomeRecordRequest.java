package com.fml.fluxa.income.application.dto;

import com.fml.fluxa.income.domain.model.IncomeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateIncomeRecordRequest(
        @NotNull @DecimalMin("0.00")
        BigDecimal amount,

        @NotNull
        IncomeStatus status,

        LocalDate receivedDate,

        String notes
) {}
