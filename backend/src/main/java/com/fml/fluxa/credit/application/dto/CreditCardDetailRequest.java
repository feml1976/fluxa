package com.fml.fluxa.credit.application.dto;

import com.fml.fluxa.credit.domain.model.CardBrand;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreditCardDetailRequest(
        @NotBlank @Size(min = 4, max = 4) String cardNumberLast4,
        @NotNull CardBrand brand,
        @NotNull @DecimalMin("0.00") BigDecimal creditLimitPurchases,
        @NotNull @DecimalMin("0.00") BigDecimal creditLimitAdvances,
        @NotNull @DecimalMin("0.00") BigDecimal availablePurchases,
        @NotNull @DecimalMin("0.00") BigDecimal availableAdvances,
        @NotNull @DecimalMin("0.00") BigDecimal previousBalance,
        @NotNull @DecimalMin("0.00") BigDecimal minimumPayment,
        @NotNull @DecimalMin("0.00") BigDecimal alternateMinimumPayment,
        @NotNull @DecimalMin("0.00") BigDecimal lateInterest,
        @NotNull @Min(1) @Max(31) int paymentDueDay
) {}
