package com.fml.fluxa.income.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyIncomeSummary(
        int month,
        int year,
        BigDecimal totalExpected,
        BigDecimal totalReceived,
        List<IncomeRecordResponse> records
) {}
