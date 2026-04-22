package com.fml.fluxa.credit.application.dto;

import com.fml.fluxa.credit.domain.model.CreditPayment;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditPaymentResponse(
        Long id,
        Long creditId,
        int periodMonth,
        int periodYear,
        BigDecimal amount,
        LocalDate paymentDate,
        String notes
) {
    public static CreditPaymentResponse from(CreditPayment p) {
        return new CreditPaymentResponse(
                p.getId(), p.getCreditId(),
                p.getPeriodMonth(), p.getPeriodYear(),
                p.getAmount(), p.getPaymentDate(), p.getNotes()
        );
    }
}
