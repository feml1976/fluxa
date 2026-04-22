package com.fml.fluxa.credit.application.dto;

import java.math.BigDecimal;

public record AmortizationRow(
        int installmentNumber,
        BigDecimal installmentAmount,
        BigDecimal interestPortion,
        BigDecimal capitalPortion,
        BigDecimal remainingBalance
) {}
