package com.fml.fluxa.notification.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notification_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private NotificationEventType eventType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_name", length = 200)
    private String referenceName;

    @Column(nullable = false, length = 200)
    private String recipient;

    @Column(nullable = false, length = 300)
    private String subject;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) sentAt = Instant.now();
    }
}
