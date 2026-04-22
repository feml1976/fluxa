package com.fml.fluxa.commitment.application.dto;

import com.fml.fluxa.commitment.domain.model.CommitmentFrequency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FixedCommitmentRequest(
        @NotBlank @Size(min = 2, max = 150)
        String name,

        @Size(max = 500)
        String description,

        @NotNull @DecimalMin("0.00")
        BigDecimal estimatedAmount,

        @NotNull @Min(1) @Max(31)
        Integer dueDay,

        @NotNull
        CommitmentFrequency frequency,

        @Min(1) @Max(30)
        Integer alertDaysBefore,

        Long categoryId
) {}
