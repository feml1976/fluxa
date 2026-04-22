package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;

public record DebtStrategyItem(
        int priority,
        Long creditId,
        String creditName,
        String creditType,
        BigDecimal currentBalance,
        BigDecimal interestRateMv,
        BigDecimal monthlyPayment,
        int estimatedMonths,
        BigDecimal totalInterestToPay
) {}
