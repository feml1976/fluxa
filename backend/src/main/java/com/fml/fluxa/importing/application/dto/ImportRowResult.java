package com.fml.fluxa.importing.application.dto;

import java.util.List;

public record ImportRowResult(
        int rowNumber,
        String rawData,
        boolean valid,
        List<String> errors,
        String resolvedType,
        String resolvedDescription,
        String resolvedAmount,
        String resolvedDate
) {}
