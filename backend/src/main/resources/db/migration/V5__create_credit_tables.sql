-- ============================================================
-- V5: Módulo Créditos y Deudas (M5)
-- ============================================================

-- Tabla principal de créditos (todos los tipos)
CREATE TABLE credits (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES users(id),
    type                 VARCHAR(20) NOT NULL
                             CHECK (type IN ('CREDIT_CARD','PERSONAL','MORTGAGE','VEHICLE')),
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                             CHECK (status IN ('ACTIVE','PAID','REFINANCED','CANCELLED')),
    name                 VARCHAR(200) NOT NULL,
    description          VARCHAR(500),
    interest_rate_mv     NUMERIC(8,4) NOT NULL DEFAULT 0,  -- Tasa Mensual Vencida (%)
    current_balance      NUMERIC(15,2) NOT NULL DEFAULT 0,
    monthly_installment  NUMERIC(15,2),                    -- cuota mensual (créditos)
    total_installments   INTEGER,                          -- plazo total en cuotas
    paid_installments    INTEGER NOT NULL DEFAULT 0,       -- cuotas pagadas
    opening_date         DATE NOT NULL,
    closing_date         DATE,                             -- fecha pago total proyectada
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ,
    deleted_by           BIGINT REFERENCES users(id)
);

-- Datos adicionales exclusivos para tarjetas de crédito
CREATE TABLE credit_cards (
    id                          BIGSERIAL PRIMARY KEY,
    credit_id                   BIGINT NOT NULL REFERENCES credits(id),
    user_id                     BIGINT NOT NULL REFERENCES users(id),
    card_number_last4           CHAR(4) NOT NULL,
    brand                       VARCHAR(20) NOT NULL DEFAULT 'VISA'
                                    CHECK (brand IN ('VISA','MASTERCARD','AMEX','DINERS','OTHER')),
    credit_limit_purchases      NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit_limit_advances       NUMERIC(15,2) NOT NULL DEFAULT 0,
    available_purchases         NUMERIC(15,2) NOT NULL DEFAULT 0,
    available_advances          NUMERIC(15,2) NOT NULL DEFAULT 0,
    previous_balance            NUMERIC(15,2) NOT NULL DEFAULT 0,
    minimum_payment             NUMERIC(15,2) NOT NULL DEFAULT 0,
    alternate_minimum_payment   NUMERIC(15,2) NOT NULL DEFAULT 0,
    late_interest               NUMERIC(15,2) NOT NULL DEFAULT 0,
    payment_due_day             INTEGER NOT NULL DEFAULT 1
                                    CHECK (payment_due_day BETWEEN 1 AND 31),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_credit_cards_credit_id UNIQUE (credit_id)
);

-- Registros de pagos por crédito / período
CREATE TABLE credit_payments (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id),
    credit_id      BIGINT NOT NULL REFERENCES credits(id),
    period_month   INTEGER NOT NULL CHECK (period_month BETWEEN 1 AND 12),
    period_year    INTEGER NOT NULL CHECK (period_year >= 2000),
    amount         NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    payment_date   DATE NOT NULL,
    notes          VARCHAR(500),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_credit_payment_period UNIQUE (credit_id, period_month, period_year)
);

-- Índices
CREATE INDEX idx_credits_user_id       ON credits(user_id);
CREATE INDEX idx_credits_deleted_at    ON credits(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_credits_type          ON credits(type);
CREATE INDEX idx_credit_cards_user_id  ON credit_cards(user_id);
CREATE INDEX idx_credit_payments_credit ON credit_payments(credit_id);
CREATE INDEX idx_credit_payments_user   ON credit_payments(user_id);

-- Trigger updated_at
CREATE TRIGGER trg_credits_updated_at
    BEFORE UPDATE ON credits
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

CREATE TRIGGER trg_credit_cards_updated_at
    BEFORE UPDATE ON credit_cards
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

CREATE TRIGGER trg_credit_payments_updated_at
    BEFORE UPDATE ON credit_payments
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();
