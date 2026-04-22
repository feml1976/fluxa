package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingPaymentDto(
        Long commitmentId,
        String name,
        BigDecimal estimatedAmount,
        LocalDate dueDate,
        int daysUntilDue
) {}
