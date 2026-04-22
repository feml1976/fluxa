package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;

public record MonthlyProjectionDto(
        int month,
        int year,
        String label,
        BigDecimal projectedIncome,
        BigDecimal projectedCommitments,
        BigDecimal projectedVariableExpenses,
        BigDecimal projectedCreditObligations,
        BigDecimal projectedNetFlow
) {}
