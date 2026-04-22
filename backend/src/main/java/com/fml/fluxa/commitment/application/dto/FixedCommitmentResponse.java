package com.fml.fluxa.commitment.application.dto;

import com.fml.fluxa.commitment.domain.model.CommitmentFrequency;
import com.fml.fluxa.commitment.domain.model.FixedCommitment;
import java.math.BigDecimal;

public record FixedCommitmentResponse(
        Long id, String name, String description,
        BigDecimal estimatedAmount, int dueDay,
        CommitmentFrequency frequency, int alertDaysBefore,
        boolean isActive, Long categoryId
) {
    public static FixedCommitmentResponse from(FixedCommitment c) {
        return new FixedCommitmentResponse(
                c.getId(), c.getName(), c.getDescription(),
                c.getEstimatedAmount(), c.getDueDay(),
                c.getFrequency(), c.getAlertDaysBefore(),
                c.isActive(), c.getCategoryId()
        );
    }
}
