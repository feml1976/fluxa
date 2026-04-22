package com.fml.fluxa.credit.application.dto;

import com.fml.fluxa.credit.domain.model.CreditStatus;
import com.fml.fluxa.credit.domain.model.CreditType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditRequest(
        @NotNull CreditType type,
        CreditStatus status,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.0000") BigDecimal interestRateMv,
        @NotNull @DecimalMin("0.00") BigDecimal currentBalance,
        @DecimalMin("0.00") BigDecimal monthlyInstallment,
        @Min(1) Integer totalInstallments,
        @Min(0) Integer paidInstallments,
        @NotNull LocalDate openingDate,
        LocalDate closingDate,

        // Solo requerido si type == CREDIT_CARD
        @Valid CreditCardDetailRequest cardDetail
) {}
