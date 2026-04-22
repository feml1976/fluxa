-- Corrección: columnas restantes SMALLINT → INTEGER en fixed_commitments
ALTER TABLE fixed_commitments ALTER COLUMN due_day           TYPE INTEGER;
ALTER TABLE fixed_commitments ALTER COLUMN alert_days_before TYPE INTEGER;
