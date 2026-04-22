-- ============================================================
-- V4 — Módulo Gastos Variables: gastos y planes de presupuesto
-- Nota: expense_categories ya existe desde V3
-- ============================================================

CREATE TABLE variable_expenses (
    id           BIGSERIAL     PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users(id),
    category_id  BIGINT        REFERENCES expense_categories(id),
    amount       NUMERIC(15,2) NOT NULL,
    expense_date DATE          NOT NULL,
    description  VARCHAR(500),
    tags         TEXT,
    receipt_url  VARCHAR(500),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ,
    deleted_by   BIGINT,

    CONSTRAINT variable_expenses_amount_positive CHECK (amount > 0)
);

CREATE TABLE budget_plans (
    id               BIGSERIAL     PRIMARY KEY,
    user_id          BIGINT        NOT NULL REFERENCES users(id),
    category_id      BIGINT        NOT NULL REFERENCES expense_categories(id),
    planned_amount   NUMERIC(15,2) NOT NULL DEFAULT 0,
    period_month     INTEGER       NOT NULL,
    period_year      INTEGER       NOT NULL,
    suggested_amount NUMERIC(15,2),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT budget_plans_month_check  CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT budget_plans_unique       UNIQUE (user_id, category_id, period_month, period_year)
);

-- Índices
CREATE INDEX idx_variable_expenses_user     ON variable_expenses(user_id);
CREATE INDEX idx_variable_expenses_date     ON variable_expenses(user_id, expense_date);
CREATE INDEX idx_variable_expenses_category ON variable_expenses(user_id, category_id);
CREATE INDEX idx_variable_expenses_del      ON variable_expenses(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_budget_plans_user_period   ON budget_plans(user_id, period_year, period_month);

-- Trigger updated_at
CREATE TRIGGER trg_variable_expenses_updated_at
    BEFORE UPDATE ON variable_expenses
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

-- Categorías de gasto variable por defecto para el admin
INSERT INTO expense_categories (user_id, name, color, icon, type) VALUES
    (1, 'Alimentación',   '#e65100', 'restaurant',       'VARIABLE'),
    (1, 'Entretenimiento','#1565c0', 'movie',            'VARIABLE'),
    (1, 'Ropa y Calzado', '#6a1b9a', 'checkroom',        'VARIABLE'),
    (1, 'Salud Variable', '#c62828', 'medical_services', 'VARIABLE'),
    (1, 'Otros Gastos',   '#37474f', 'shopping_bag',     'VARIABLE');
