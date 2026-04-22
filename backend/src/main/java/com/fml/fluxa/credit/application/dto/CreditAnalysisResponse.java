package com.fml.fluxa.credit.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreditAnalysisResponse(
        Long creditId,
        CreditAlertLevel alertLevel,
        List<String> alerts,

        // Tarjeta de crédito
        BigDecimal utilizationPct,
        Integer monthsToPayMinimum,
        BigDecimal totalInterestWithMinimum,
        boolean alternateMinimumWarning,

        // Créditos (personal/hipoteca/vehículo)
        Integer remainingInstallments,
        LocalDate projectedPayoffDate,
        BigDecimal totalRemainingInterest,
        List<AmortizationRow> amortizationTable
) {}
