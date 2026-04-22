package com.fml.fluxa.credit.application.dto;

import com.fml.fluxa.credit.domain.model.Credit;
import com.fml.fluxa.credit.domain.model.CreditStatus;
import com.fml.fluxa.credit.domain.model.CreditType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditResponse(
        Long id,
        CreditType type,
        CreditStatus status,
        String name,
        String description,
        BigDecimal interestRateMv,
        BigDecimal interestRateEa,
        BigDecimal currentBalance,
        BigDecimal monthlyInstallment,
        Integer totalInstallments,
        int paidInstallments,
        Integer remainingInstallments,
        LocalDate openingDate,
        LocalDate closingDate,
        CreditAlertLevel alertLevel,
        CreditCardDetailResponse cardDetail
) {
    public static CreditResponse from(Credit c, CreditCardDetailResponse card,
                                      BigDecimal ea, CreditAlertLevel alert) {
        Integer remaining = (c.getTotalInstallments() != null)
                ? c.getTotalInstallments() - c.getPaidInstallments()
                : null;
        return new CreditResponse(
                c.getId(),
                c.getType(),
                c.getStatus(),
                c.getName(),
                c.getDescription(),
                c.getInterestRateMv(),
                ea,
                c.getCurrentBalance(),
                c.getMonthlyInstallment(),
                c.getTotalInstallments(),
                c.getPaidInstallments(),
                remaining,
                c.getOpeningDate(),
                c.getClosingDate(),
                alert,
                card
        );
    }
}
