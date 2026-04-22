package com.fml.fluxa.credit.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditPaymentRequest(
        @NotNull @Min(1) @Max(12) int month,
        @NotNull @Min(2000) int year,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate paymentDate,
        @Size(max = 500) String notes
) {}
