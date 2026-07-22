ALTER TABLE solicitudes_propietario
    ADD COLUMN IF NOT EXISTS documento_archivo_id VARCHAR(80),
    ADD COLUMN IF NOT EXISTS documento_nombre VARCHAR(255),
    ADD COLUMN IF NOT EXISTS documento_tipo VARCHAR(120),
    ADD COLUMN IF NOT EXISTS documento_tamano BIGINT;
