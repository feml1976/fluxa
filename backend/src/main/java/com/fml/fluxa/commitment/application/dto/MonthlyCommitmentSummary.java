package com.fml.fluxa.commitment.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyCommitmentSummary(
        int month,
        int year,
        BigDecimal totalEstimated,
        BigDecimal totalPaid,
        long pendingCount,
        long paidCount,
        long overdueCount,
        List<CommitmentRecordResponse> records
) {}
