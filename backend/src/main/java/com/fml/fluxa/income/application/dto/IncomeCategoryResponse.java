package com.fml.fluxa.income.application.dto;

import com.fml.fluxa.income.domain.model.IncomeCategory;

public record IncomeCategoryResponse(Long id, String name, String color, String icon) {

    public static IncomeCategoryResponse from(IncomeCategory c) {
        return new IncomeCategoryResponse(c.getId(), c.getName(), c.getColor(), c.getIcon());
    }
}
