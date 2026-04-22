-- Corrección: period_month y period_year deben ser INTEGER (no SMALLINT)
-- para compatibilidad con el tipo 'int' de Hibernate/Java
ALTER TABLE income_records     ALTER COLUMN period_month TYPE INTEGER;
ALTER TABLE income_records     ALTER COLUMN period_year  TYPE INTEGER;
ALTER TABLE commitment_records ALTER COLUMN period_month TYPE INTEGER;
ALTER TABLE commitment_records ALTER COLUMN period_year  TYPE INTEGER;
