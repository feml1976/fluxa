package com.fml.fluxa.notification.application.dto;

import com.fml.fluxa.notification.domain.model.NotificationEventType;
import com.fml.fluxa.notification.domain.model.NotificationLog;
import java.time.Instant;

public record NotificationLogResponse(
        Long id,
        NotificationEventType eventType,
        String referenceName,
        String recipient,
        String subject,
        Instant sentAt,
        boolean success,
        String errorMessage
) {
    public static NotificationLogResponse from(NotificationLog n) {
        return new NotificationLogResponse(
                n.getId(), n.getEventType(), n.getReferenceName(),
                n.getRecipient(), n.getSubject(), n.getSentAt(),
                n.isSuccess(), n.getErrorMessage()
        );
    }
}
