package com.fml.fluxa.credit.application.dto;

import java.math.BigDecimal;

public record CreditSummaryResponse(
        int totalCredits,
        int activeCredits,
        BigDecimal totalDebt,
        BigDecimal totalMonthlyObligations,
        BigDecimal cardUsedBalance,
        BigDecimal cardMinimumPayments,
        int cardsWithLateInterest,
        int cardsAtMaxCapacity
) {}
