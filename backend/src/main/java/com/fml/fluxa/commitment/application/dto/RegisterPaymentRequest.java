package com.fml.fluxa.commitment.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterPaymentRequest(
        @NotNull @DecimalMin("0.01")
        BigDecimal actualAmount,

        LocalDate paidDate,

        String receiptReference,

        String notes
) {}
