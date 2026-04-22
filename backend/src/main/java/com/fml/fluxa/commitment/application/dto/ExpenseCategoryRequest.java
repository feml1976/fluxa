package com.fml.fluxa.commitment.application.dto;

import com.fml.fluxa.expense.domain.model.ExpenseCategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExpenseCategoryRequest(
        @NotBlank @Size(min = 2, max = 100)
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color debe ser hexadecimal (#RRGGBB)")
        String color,

        @Size(max = 50)
        String icon,

        @NotNull
        ExpenseCategoryType type
) {}
