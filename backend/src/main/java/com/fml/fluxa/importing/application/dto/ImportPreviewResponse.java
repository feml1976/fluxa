package com.fml.fluxa.importing.application.dto;

import java.util.List;

public record ImportPreviewResponse(
        int totalRows,
        int validRows,
        int invalidRows,
        List<ImportRowResult> rows
) {}
