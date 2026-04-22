package com.fml.fluxa.income.application.dto;

import com.fml.fluxa.income.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeSourceResponse(
        Long id, String name, String description,
        IncomeType type, BigDecimal expectedAmount,
        IncomeFrequency frequency, LocalDate startDate, LocalDate endDate,
        boolean isActive, Long categoryId
) {
    public static IncomeSourceResponse from(IncomeSource s) {
        return new IncomeSourceResponse(
                s.getId(), s.getName(), s.getDescription(),
                s.getType(), s.getExpectedAmount(),
                s.getFrequency(), s.getStartDate(), s.getEndDate(),
                s.isActive(), s.getCategoryId()
        );
    }
}
