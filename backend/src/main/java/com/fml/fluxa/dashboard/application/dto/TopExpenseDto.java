package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;

public record TopExpenseDto(
        Long categoryId,
        String categoryName,
        BigDecimal total,
        double percentage
) {}
