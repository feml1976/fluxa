package com.fml.fluxa.credit.application.dto;

import com.fml.fluxa.credit.domain.model.CardBrand;
import com.fml.fluxa.credit.domain.model.CreditCard;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record CreditCardDetailResponse(
        String cardNumberLast4,
        CardBrand brand,
        BigDecimal creditLimitPurchases,
        BigDecimal creditLimitAdvances,
        BigDecimal availablePurchases,
        BigDecimal availableAdvances,
        BigDecimal previousBalance,
        BigDecimal minimumPayment,
        BigDecimal alternateMinimumPayment,
        BigDecimal lateInterest,
        int paymentDueDay,
        BigDecimal utilizationPct
) {
    public static CreditCardDetailResponse from(CreditCard cc) {
        BigDecimal used = cc.getCreditLimitPurchases().subtract(cc.getAvailablePurchases());
        BigDecimal pct = cc.getCreditLimitPurchases().compareTo(BigDecimal.ZERO) > 0
                ? used.multiply(new BigDecimal("100"))
                        .divide(cc.getCreditLimitPurchases(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new CreditCardDetailResponse(
                cc.getCardNumberLast4(),
                cc.getBrand(),
                cc.getCreditLimitPurchases(),
                cc.getCreditLimitAdvances(),
                cc.getAvailablePurchases(),
                cc.getAvailableAdvances(),
                cc.getPreviousBalance(),
                cc.getMinimumPayment(),
                cc.getAlternateMinimumPayment(),
                cc.getLateInterest(),
                cc.getPaymentDueDay(),
                pct
        );
    }
}
