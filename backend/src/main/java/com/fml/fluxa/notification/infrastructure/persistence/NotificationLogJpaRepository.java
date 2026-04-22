package com.fml.fluxa.notification.infrastructure.persistence;

import com.fml.fluxa.notification.domain.model.NotificationEventType;
import com.fml.fluxa.notification.domain.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NotificationLogJpaRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId);

    @Query("SELECT COUNT(n) > 0 FROM NotificationLog n " +
           "WHERE n.userId = :userId AND n.eventType = :type " +
           "AND n.referenceId = :refId AND n.success = true " +
           "AND n.sentAt >= :since")
    boolean existsRecentNotification(
            @Param("userId") Long userId,
            @Param("type") NotificationEventType type,
            @Param("refId") Long refId,
            @Param("since") Instant since);
}
