-- ============================================================
-- V2 — Módulo Ingresos: categorías, fuentes, registros
-- ============================================================

CREATE TABLE income_categories (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    name       VARCHAR(100) NOT NULL,
    color      VARCHAR(7)   NOT NULL DEFAULT '#1976d2',
    icon       VARCHAR(50),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT
);

CREATE TABLE income_sources (
    id              BIGSERIAL      PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users(id),
    category_id     BIGINT         REFERENCES income_categories(id),
    name            VARCHAR(150)   NOT NULL,
    description     VARCHAR(500),
    type            VARCHAR(10)    NOT NULL,
    expected_amount NUMERIC(15,2)  NOT NULL DEFAULT 0,
    frequency       VARCHAR(10)    NOT NULL DEFAULT 'MONTHLY',
    start_date      DATE           NOT NULL,
    end_date        DATE,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    deleted_by      BIGINT,

    CONSTRAINT income_sources_type_check      CHECK (type      IN ('FIXED','VARIABLE')),
    CONSTRAINT income_sources_frequency_check CHECK (frequency IN ('MONTHLY','BIWEEKLY','WEEKLY','ONE_TIME'))
);

CREATE TABLE income_records (
    id            BIGSERIAL     PRIMARY KEY,
    user_id       BIGINT        NOT NULL REFERENCES users(id),
    source_id     BIGINT        NOT NULL REFERENCES income_sources(id),
    amount        NUMERIC(15,2) NOT NULL DEFAULT 0,
    received_date DATE,
    period_month  SMALLINT      NOT NULL,
    period_year   SMALLINT      NOT NULL,
    status        VARCHAR(15)   NOT NULL DEFAULT 'EXPECTED',
    notes         VARCHAR(500),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT income_records_status_check       CHECK (status IN ('EXPECTED','RECEIVED','PARTIAL','NOT_RECEIVED')),
    CONSTRAINT income_records_month_check        CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT income_records_unique_period      UNIQUE (source_id, period_month, period_year)
);

-- Índices
CREATE INDEX idx_income_categories_user  ON income_categories(user_id);
CREATE INDEX idx_income_categories_del   ON income_categories(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_income_sources_user     ON income_sources(user_id);
CREATE INDEX idx_income_sources_del      ON income_sources(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_income_records_user     ON income_records(user_id);
CREATE INDEX idx_income_records_period   ON income_records(user_id, period_year, period_month);
CREATE INDEX idx_income_records_source   ON income_records(source_id);

-- Triggers updated_at
CREATE TRIGGER trg_income_categories_updated_at
    BEFORE UPDATE ON income_categories
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

CREATE TRIGGER trg_income_sources_updated_at
    BEFORE UPDATE ON income_sources
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

-- Categorías de ingreso por defecto para el admin
INSERT INTO income_categories (user_id, name, color, icon) VALUES
    (1, 'Salario',      '#1976d2', 'work'),
    (1, 'Honorarios',   '#388e3c', 'assignment'),
    (1, 'Arriendo',     '#f57c00', 'home'),
    (1, 'Inversiones',  '#7b1fa2', 'trending_up'),
    (1, 'Otros',        '#616161', 'attach_money');
