-- Inicialización de la base de datos FLUXA
-- Este script solo se ejecuta si el volumen está vacío

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Zona horaria por defecto para la sesión
SET timezone = 'America/Bogota';
