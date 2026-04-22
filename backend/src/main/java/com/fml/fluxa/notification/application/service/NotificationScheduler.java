package com.fml.fluxa.notification.application.service;

import com.fml.fluxa.auth.infrastructure.persistence.UserJpaRepository;
import com.fml.fluxa.commitment.domain.model.CommitmentStatus;
import com.fml.fluxa.commitment.infrastructure.persistence.CommitmentRecordJpaRepository;
import com.fml.fluxa.credit.domain.model.CreditStatus;
import com.fml.fluxa.credit.infrastructure.persistence.CreditCardJpaRepository;
import com.fml.fluxa.credit.infrastructure.persistence.CreditJpaRepository;
import com.fml.fluxa.notification.domain.model.NotificationEventType;
import com.fml.fluxa.notification.infrastructure.persistence.NotificationLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final UserJpaRepository userRepo;
    private final CommitmentRecordJpaRepository commitmentRecordRepo;
    private final CreditJpaRepository creditRepo;
    private final CreditCardJpaRepository creditCardRepo;
    private final EmailNotificationService emailService;
    private final NotificationLogJpaRepository logRepo;

    @Value("${fluxa.notification.alert-days-before:3}")
    private int alertDaysBefore;

    public NotificationScheduler(UserJpaRepository userRepo,
                                  CommitmentRecordJpaRepository commitmentRecordRepo,
                                  CreditJpaRepository creditRepo,
                                  CreditCardJpaRepository creditCardRepo,
                                  EmailNotificationService emailService,
                                  NotificationLogJpaRepository logRepo) {
        this.userRepo = userRepo;
        this.commitmentRecordRepo = commitmentRecordRepo;
        this.creditRepo = creditRepo;
        this.creditCardRepo = creditCardRepo;
        this.emailService = emailService;
        this.logRepo = logRepo;
    }

    @Scheduled(cron = "${fluxa.notification.scheduler-cron:0 0 8 * * *}",
               zone = "America/Bogota")
    public void runDailyNotifications() {
        log.info("Iniciando job de notificaciones diarias");
        LocalDate today = LocalDate.now(BOGOTA);

        userRepo.findAll().stream()
                .filter(u -> u.isActive() && u.getDeletedAt() == null)
                .forEach(user -> {
                    try {
                        checkCommitmentsForUser(user.getId(), user.getEmail(),
                                user.getFirstName(), today);
                        checkCreditCardsForUser(user.getId(), user.getEmail(),
                                user.getFirstName());
                    } catch (Exception e) {
                        log.error("Error procesando notificaciones para usuario {}: {}",
                                user.getId(), e.getMessage());
                    }
                });

        log.info("Job de notificaciones diarias completado");
    }

    private void checkCommitmentsForUser(Long userId, String email,
                                          String firstName, LocalDate today) {
        LocalDate alertFrom = today;
        LocalDate alertTo   = today.plusDays(alertDaysBefore);

        // Vencimientos próximos
        commitmentRecordRepo.findUpcomingPending(userId, alertFrom, alertTo)
                .forEach(record -> {
                    if (alreadyNotifiedToday(userId, NotificationEventType.COMMITMENT_DUE_SOON,
                            record.getCommitmentId())) return;

                    int days = (int) ChronoUnit.DAYS.between(today, record.getDueDate());
                    String amount = "$" + record.getEstimatedAmount().toBigInteger();
                    String subject = days == 0
                            ? "⚠️ FLUXA: Pago vence HOY"
                            : "⚠️ FLUXA: Pago vence en " + days + " día(s)";

                    emailService.sendAndLog(userId,
                            NotificationEventType.COMMITMENT_DUE_SOON,
                            record.getCommitmentId(), null,
                            email, subject,
                            emailService.buildCommitmentDueSoonHtml(
                                    firstName, "Compromiso #" + record.getCommitmentId(),
                                    record.getDueDate().toString(), amount, days));
                });

        // Compromisos vencidos sin pagar
        commitmentRecordRepo.findByUserIdAndStatus(userId, CommitmentStatus.OVERDUE)
                .forEach(record -> {
                    if (alreadyNotifiedToday(userId, NotificationEventType.COMMITMENT_OVERDUE,
                            record.getCommitmentId())) return;

                    emailService.sendAndLog(userId,
                            NotificationEventType.COMMITMENT_OVERDUE,
                            record.getCommitmentId(), null,
                            email, "🔴 FLUXA: Tienes un compromiso VENCIDO sin pagar",
                            emailService.buildCommitmentDueSoonHtml(
                                    firstName, "Compromiso #" + record.getCommitmentId(),
                                    record.getDueDate().toString(),
                                    "$" + record.getEstimatedAmount().toBigInteger(), -1));
                });
    }

    private void checkCreditCardsForUser(Long userId, String email, String firstName) {
        creditRepo.findByUserIdAndStatusAndDeletedAtIsNull(userId, CreditStatus.ACTIVE)
                .forEach(credit -> creditCardRepo.findByCreditId(credit.getId())
                        .ifPresent(card -> {
                            // Intereses de mora
                            if (card.getLateInterest().compareTo(BigDecimal.ZERO) > 0) {
                                if (!alreadyNotifiedToday(userId,
                                        NotificationEventType.CREDIT_CARD_LATE_INTEREST,
                                        credit.getId())) {
                                    String detail = "Intereses de mora: $" +
                                            card.getLateInterest().toBigInteger();
                                    emailService.sendAndLog(userId,
                                            NotificationEventType.CREDIT_CARD_LATE_INTEREST,
                                            credit.getId(), credit.getName(),
                                            email, "🚨 FLUXA: Intereses de mora en tu tarjeta",
                                            emailService.buildCreditCardAlertHtml(
                                                    firstName, credit.getName(),
                                                    "Tienes intereses de mora activos", detail));
                                }
                            }

                            // Cupo agotado
                            if (card.getAvailablePurchases().compareTo(BigDecimal.ZERO) <= 0) {
                                if (!alreadyNotifiedToday(userId,
                                        NotificationEventType.CREDIT_CARD_NO_AVAILABLE,
                                        credit.getId())) {
                                    emailService.sendAndLog(userId,
                                            NotificationEventType.CREDIT_CARD_NO_AVAILABLE,
                                            credit.getId(), credit.getName(),
                                            email, "🚨 FLUXA: Cupo agotado en tu tarjeta",
                                            emailService.buildCreditCardAlertHtml(
                                                    firstName, credit.getName(),
                                                    "Cupo disponible agotado",
                                                    "No tienes cupo disponible para compras"));
                                }
                            }
                        }));
    }

    private boolean alreadyNotifiedToday(Long userId, NotificationEventType type, Long refId) {
        Instant startOfDay = LocalDate.now(BOGOTA).atStartOfDay(BOGOTA).toInstant();
        return logRepo.existsRecentNotification(userId, type, refId, startOfDay);
    }
}
