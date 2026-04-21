-- ============================================================
-- V1 — Módulo Auth: grupos, usuarios, tokens de recuperación
-- FLUXA | Flyway Migration
-- ============================================================

-- Tabla de grupos (núcleo familiar / hogar)
CREATE TABLE groups (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Tabla de usuarios
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'USER',
    group_id      BIGINT       REFERENCES groups(id),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ,
    deleted_by    BIGINT,

    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_role_check   CHECK  (role IN ('ADMIN', 'USER'))
);

-- Tabla de refresh tokens (para invalidación en logout)
CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT refresh_tokens_hash_unique UNIQUE (token_hash)
);

-- Tabla de tokens de recuperación de contraseña
CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT prt_hash_unique UNIQUE (token_hash)
);

-- ── Índices ───────────────────────────────────────────────────
CREATE INDEX idx_users_email        ON users(email);
CREATE INDEX idx_users_group_id     ON users(group_id);
CREATE INDEX idx_users_deleted_at   ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_prt_user_id        ON password_reset_tokens(user_id);

-- ── Función para actualizar updated_at automáticamente ────────
CREATE OR REPLACE FUNCTION fn_update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

CREATE TRIGGER trg_groups_updated_at
    BEFORE UPDATE ON groups
    FOR EACH ROW EXECUTE FUNCTION fn_update_updated_at();

-- ── Datos semilla: grupo por defecto + usuario ADMIN ──────────
INSERT INTO groups (name, description)
VALUES ('Familia Principal', 'Grupo familiar por defecto');

-- Contraseña: Admin1234! (BCrypt strength=12 — cambiar en primer login)
INSERT INTO users (email, password_hash, first_name, last_name, role, group_id)
VALUES (
    'admin@fluxa.local',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCgT9XR1B0lHU5FGW3CZuLu',
    'Admin',
    'FLUXA',
    'ADMIN',
    1
);
