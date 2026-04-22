-- Corrección: CHAR(4) en PostgreSQL es bpchar, incompatible con varchar de Hibernate
ALTER TABLE credit_cards ALTER COLUMN card_number_last4 TYPE VARCHAR(4);
