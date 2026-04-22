package com.fml.fluxa.notification.application.service;

import com.fml.fluxa.notification.domain.model.NotificationEventType;
import com.fml.fluxa.notification.domain.model.NotificationLog;
import com.fml.fluxa.notification.infrastructure.persistence.NotificationLogJpaRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Bogota"));

    private final JavaMailSender mailSender;
    private final NotificationLogJpaRepository logRepo;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${fluxa.notification.from-name:FLUXA — Gestión Financiera}")
    private String fromName;

    public EmailNotificationService(JavaMailSender mailSender,
                                    NotificationLogJpaRepository logRepo) {
        this.mailSender = mailSender;
        this.logRepo = logRepo;
    }

    public void sendAndLog(Long userId, NotificationEventType eventType,
                            Long referenceId, String referenceName,
                            String toEmail, String subject, String htmlBody) {
        boolean success = true;
        String error = null;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Notificación enviada: tipo={} referencia={} destinatario={}", eventType, referenceName, toEmail);
        } catch (Exception e) {
            success = false;
            error = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 490)) : "Error desconocido";
            log.error("Error enviando notificación tipo={}: {}", eventType, e.getMessage());
        }

        logRepo.save(NotificationLog.builder()
                .userId(userId)
                .eventType(eventType)
                .referenceId(referenceId)
                .referenceName(referenceName)
                .recipient(toEmail)
                .subject(subject)
                .sentAt(Instant.now())
                .success(success)
                .errorMessage(error)
                .build());
    }

    // ── Plantillas HTML ──────────────────────────────────────────

    public String buildCommitmentDueSoonHtml(String userName, String commitmentName,
                                              String dueDate, String amount, int daysLeft) {
        String urgency = daysLeft == 0 ? "¡VENCE HOY!" : "Vence en " + daysLeft + " día(s)";
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <div style="background:#1976d2;padding:20px;border-radius:8px 8px 0 0">
                <h2 style="color:white;margin:0">💰 FLUXA — Alerta de Vencimiento</h2>
              </div>
              <div style="padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 8px 8px">
                <p>Hola <strong>%s</strong>,</p>
                <div style="background:#fff3e0;border-left:4px solid #ed6c02;padding:16px;border-radius:4px;margin:16px 0">
                  <p style="margin:0;font-size:18px;font-weight:bold;color:#e65100">%s</p>
                  <p style="margin:8px 0 0">Compromiso: <strong>%s</strong></p>
                  <p style="margin:4px 0">Monto estimado: <strong>%s</strong></p>
                  <p style="margin:4px 0">Fecha límite: <strong>%s</strong></p>
                </div>
                <p>Ingresa a FLUXA para registrar el pago y mantener tus finanzas al día.</p>
                <p style="color:#9e9e9e;font-size:12px;margin-top:32px">FLUXA — Gestión Financiera Personal</p>
              </div>
            </div>
            """.formatted(userName, urgency, commitmentName, amount, dueDate);
    }

    public String buildCreditCardAlertHtml(String userName, String cardName,
                                            String alertType, String detail) {
        String color = "#d32f2f";
        String icon  = "🚨";
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <div style="background:%s;padding:20px;border-radius:8px 8px 0 0">
                <h2 style="color:white;margin:0">%s FLUXA — Alerta de Tarjeta</h2>
              </div>
              <div style="padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 8px 8px">
                <p>Hola <strong>%s</strong>,</p>
                <div style="background:#ffebee;border-left:4px solid #d32f2f;padding:16px;border-radius:4px;margin:16px 0">
                  <p style="margin:0;font-size:16px;font-weight:bold;color:#b71c1c">%s</p>
                  <p style="margin:8px 0 0">Tarjeta: <strong>%s</strong></p>
                  <p style="margin:4px 0">%s</p>
                </div>
                <p>Revisa tu tarjeta en FLUXA y toma acción inmediata.</p>
                <p style="color:#9e9e9e;font-size:12px;margin-top:32px">FLUXA — Gestión Financiera Personal</p>
              </div>
            </div>
            """.formatted(color, icon, userName, alertType, cardName, detail);
    }

    public String buildTestHtml(String userName) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <div style="background:#2e7d32;padding:20px;border-radius:8px 8px 0 0">
                <h2 style="color:white;margin:0">✅ FLUXA — Notificación de Prueba</h2>
              </div>
              <div style="padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 8px 8px">
                <p>Hola <strong>%s</strong>,</p>
                <p>Las notificaciones por email están correctamente configuradas en <strong>FLUXA</strong>.</p>
                <p>Recibirás alertas automáticas sobre:</p>
                <ul>
                  <li>Compromisos próximos a vencer</li>
                  <li>Compromisos vencidos sin pagar</li>
                  <li>Tarjetas con intereses de mora</li>
                  <li>Tarjetas con cupo agotado</li>
                </ul>
                <p style="color:#9e9e9e;font-size:12px;margin-top:32px">FLUXA — Gestión Financiera Personal</p>
              </div>
            </div>
            """.formatted(userName);
    }
}
