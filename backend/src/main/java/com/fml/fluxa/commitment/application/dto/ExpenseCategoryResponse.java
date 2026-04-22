package com.fml.fluxa.commitment.application.dto;

import com.fml.fluxa.expense.domain.model.ExpenseCategory;
import com.fml.fluxa.expense.domain.model.ExpenseCategoryType;

public record ExpenseCategoryResponse(Long id, String name, String color, String icon, ExpenseCategoryType type) {

    public static ExpenseCategoryResponse from(ExpenseCategory c) {
        return new ExpenseCategoryResponse(c.getId(), c.getName(), c.getColor(), c.getIcon(), c.getType());
    }
}
