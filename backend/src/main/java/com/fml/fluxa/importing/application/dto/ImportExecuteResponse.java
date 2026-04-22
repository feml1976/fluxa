package com.fml.fluxa.importing.application.dto;

public record ImportExecuteResponse(
        int imported,
        int skipped,
        String message
) {}
