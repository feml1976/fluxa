package com.fml.fluxa.expense.application.dto;

import com.fml.fluxa.expense.domain.model.VariableExpense;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VariableExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        LocalDate expenseDate,
        String description,
        List<String> tags,
        String receiptUrl
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static VariableExpenseResponse from(VariableExpense e, String categoryName) {
        List<String> tagList = parseTags(e.getTags());
        return new VariableExpenseResponse(
                e.getId(),
                e.getCategoryId(),
                categoryName,
                e.getAmount(),
                e.getExpenseDate(),
                e.getDescription(),
                tagList,
                e.getReceiptUrl()
        );
    }

    private static List<String> parseTags(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }
}
