package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProjectionsResponse(
        int months,
        List<MonthlyProjectionDto> projections,
        BigDecimal avgProjectedIncome,
        BigDecimal avgProjectedExpenses,
        BigDecimal avgProjectedNetFlow
) {}
