package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record DebtStrategyResponse(
        String strategyName,
        String description,
        List<DebtStrategyItem> items,
        BigDecimal totalDebt,
        BigDecimal totalInterestToPay,
        int estimatedMonthsToFreedom,
        BigDecimal interestSavedVsMinimum
) {}
