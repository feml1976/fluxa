package com.fml.fluxa.notification.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.notification.application.dto.NotificationLogResponse;
import com.fml.fluxa.notification.application.service.EmailNotificationService;
import com.fml.fluxa.notification.domain.model.NotificationEventType;
import com.fml.fluxa.notification.infrastructure.persistence.NotificationLogJpaRepository;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final EmailNotificationService emailService;
    private final NotificationLogJpaRepository logRepo;

    public NotificationController(EmailNotificationService emailService,
                                  NotificationLogJpaRepository logRepo) {
        this.emailService = emailService;
        this.logRepo = logRepo;
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> sendTest(
            @AuthenticationPrincipal User user) {
        emailService.sendAndLog(
                user.getId(),
                NotificationEventType.TEST,
                null, null,
                user.getEmail(),
                "✅ FLUXA — Notificación de Prueba",
                emailService.buildTestHtml(user.getFirstName()));
        return ResponseEntity.ok(ApiResponse.ok("Correo de prueba enviado a " + user.getEmail(), null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<NotificationLogResponse>>> getLogs(
            @AuthenticationPrincipal User user) {
        List<NotificationLogResponse> logs = logRepo
                .findByUserIdOrderBySentAtDesc(user.getId())
                .stream()
                .map(NotificationLogResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
