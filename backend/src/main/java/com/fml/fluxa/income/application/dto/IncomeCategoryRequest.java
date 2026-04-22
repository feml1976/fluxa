package com.fml.fluxa.income.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IncomeCategoryRequest(
        @NotBlank @Size(min = 2, max = 100)
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color debe ser hexadecimal (#RRGGBB)")
        String color,

        @Size(max = 50)
        String icon
) {}
