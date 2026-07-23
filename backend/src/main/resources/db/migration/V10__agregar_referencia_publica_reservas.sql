ALTER TABLE reservas
    ADD COLUMN IF NOT EXISTS referencia_publica VARCHAR(40);

UPDATE reservas
SET referencia_publica = 'LV-' || UPPER(REPLACE(gen_random_uuid()::TEXT, '-', ''))
WHERE referencia_publica IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_reservas_referencia_publica
    ON reservas (referencia_publica);

ALTER TABLE reservas
    ALTER COLUMN referencia_publica SET NOT NULL;
