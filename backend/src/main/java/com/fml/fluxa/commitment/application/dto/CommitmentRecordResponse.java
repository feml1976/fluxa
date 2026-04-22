package com.fml.fluxa.commitment.application.dto;

import com.fml.fluxa.commitment.domain.model.CommitmentRecord;
import com.fml.fluxa.commitment.domain.model.CommitmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CommitmentRecordResponse(
        Long id, Long commitmentId, String commitmentName,
        int periodMonth, int periodYear,
        BigDecimal estimatedAmount, BigDecimal actualAmount,
        LocalDate dueDate, LocalDate paidDate,
        CommitmentStatus status, String receiptReference, String notes
) {
    public static CommitmentRecordResponse from(CommitmentRecord r, String commitmentName) {
        return new CommitmentRecordResponse(
                r.getId(), r.getCommitmentId(), commitmentName,
                r.getPeriodMonth(), r.getPeriodYear(),
                r.getEstimatedAmount(), r.getActualAmount(),
                r.getDueDate(), r.getPaidDate(),
                r.getStatus(), r.getReceiptReference(), r.getNotes()
        );
    }
}
