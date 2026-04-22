-- ============================================================
-- V3 — Módulo Compromisos Fijos + categorías de gasto
-- (expense_categories también es usada por M4 en V4)
-- ============================================================

CREATE TABLE expense_categories (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    name       VARCHAR(100) NOT NULL,
    color      VARCHAR(7)   NOT NULL DEFAULT '#d32f2f',
    icon       VARCHAR(50),
    type       VARCHAR(10)  NOT NULL DEFAULT 'FIXED',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,

    CONSTRAINT expense_categories_type_check CHECK (type IN ('FIXED','VARIABLE'))
);

CREATE TABLE fixed_commitments (
    id                BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users(id),
    category_id       BIGINT        REFERENCES expense_categories(id),
    name              VARCHAR(150)  NOT NULL,
    description       VARCHAR(500),
    estimated_amount  NUMERIC(15,2) NOT NULL DEFAULT 0,
    due_day           SMALLINT      NOT NULL,
    frequency         VARCHAR(15)   NOT NULL DEFAULT 'MONTHLY',
    alert_days_before SMALLINT      NOT NULL DEFAULT 5,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ,
    deleted_by        BIGINT,

    CONSTRAINT fixed_commitments_due_day_check   CHECK (due_day BETWEEN 1 AND 31),
    CONSTRAINT fixed_commitments_frequency_check CHECK (frequency IN ('MONTHLY','BIMONTHLY','QUARTERLY','ANNUAL'))
);

CREATE TABLE commitment_records (
    id                BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users(id),
    commitment_id     BIGINT        NOT NULL REFERENCES fixed_commitments(id),
    period_month      SMALLINT      NOT NULL,
    period_year       SMALLINT      NOT NULL,
    estimated_amount  NUMERIC(15,2) NOT NULL DEFAULT 0,
    actual_amount     NUMERIC(15,2),
    due_date          DATE          NOT NULL,
    paid_date         DATE,
    status            VARCHAR(10)   NOT NULL DEFAULT 'PENDING',
    receipt_reference VARCHAR(200),
    notes             VARCHAR(500),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT commitment_records_status_check  CHECK (status IN ('PENDING','PAID','OVERDUE')),
    CONSTRAINT commitment_records_month_check   CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT commitment_records_unique_period UNIQUE (commitment_id, period_month, period_year)
);

-- Índices
CREATE INDEX idx_expense_categories_user ON expense_categories(user_id);
CREATE INDEX idx_expense_categories_del  ON expense_categories(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_fixed_commitments_user  ON fixed_commitments(user_id);
CREATE INDEX idx_fixed_commitments_del   ON fixed_commitments(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_commitment_records_user ON commitment_records(user_id);
CREATE INDEX idx_commitment_records_per  ON commitment_records(user_id, period_year, period_month);
CREATE INDEX idx_commitment_records_comm ON commitment_records(commitment_id);

-- Triggers updated_at
CREATE TRIGGER trg_expense_categories_updated_at
    BEFORE UPDATE ON expense_categories
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

CREATE TRIGGER trg_fixed_commitments_updated_at
    BEFORE UPDATE ON fixed_commitments
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

-- Categorías de gasto fijo por defecto para el admin
INSERT INTO expense_categories (user_id, name, color, icon, type) VALUES
    (1, 'Arriendo / Hipoteca',   '#d32f2f', 'home',             'FIXED'),
    (1, 'Servicios Públicos',    '#f57c00', 'bolt',             'FIXED'),
    (1, 'Seguros',               '#1565c0', 'security',         'FIXED'),
    (1, 'Suscripciones',         '#6a1b9a', 'subscriptions',    'FIXED'),
    (1, 'Transporte',            '#00695c', 'directions_car',   'FIXED'),
    (1, 'Educación',             '#2e7d32', 'school',           'FIXED'),
    (1, 'Salud',                 '#c62828', 'local_hospital',   'FIXED');
