-- ============================================================
-- V6: Módulo Notificaciones (M7)
-- ============================================================

CREATE TABLE notification_logs (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id),
    event_type     VARCHAR(50) NOT NULL
                       CHECK (event_type IN (
                           'COMMITMENT_DUE_SOON',
                           'COMMITMENT_OVERDUE',
                           'CREDIT_CARD_LATE_INTEREST',
                           'CREDIT_CARD_NO_AVAILABLE',
                           'TEST'
                       )),
    reference_id   BIGINT,
    reference_name VARCHAR(200),
    recipient      VARCHAR(200) NOT NULL,
    subject        VARCHAR(300) NOT NULL,
    sent_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    success        BOOLEAN NOT NULL DEFAULT TRUE,
    error_message  VARCHAR(500)
);

CREATE INDEX idx_notification_logs_user_id   ON notification_logs(user_id);
CREATE INDEX idx_notification_logs_sent_at   ON notification_logs(sent_at DESC);
CREATE INDEX idx_notification_logs_event     ON notification_logs(event_type);

-- Índice compuesto para búsquedas de deduplicación por usuario + tipo + referencia
CREATE INDEX idx_notification_logs_dedup
    ON notification_logs(user_id, event_type, reference_id, sent_at)
    WHERE success = TRUE;
