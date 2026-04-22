package com.fml.fluxa.income.application.dto;

import com.fml.fluxa.income.domain.model.IncomeRecord;
import com.fml.fluxa.income.domain.model.IncomeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeRecordResponse(
        Long id, Long sourceId, String sourceName,
        BigDecimal amount, LocalDate receivedDate,
        int periodMonth, int periodYear, IncomeStatus status, String notes
) {
    public static IncomeRecordResponse from(IncomeRecord r, String sourceName) {
        return new IncomeRecordResponse(
                r.getId(), r.getSourceId(), sourceName,
                r.getAmount(), r.getReceivedDate(),
                r.getPeriodMonth(), r.getPeriodYear(), r.getStatus(), r.getNotes()
        );
    }
}
