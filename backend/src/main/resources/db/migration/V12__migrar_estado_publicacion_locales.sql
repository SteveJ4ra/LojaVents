ALTER TABLE locales
    ADD COLUMN IF NOT EXISTS estado_publicacion VARCHAR(30);

UPDATE locales
SET estado_publicacion = CASE
    WHEN activo = TRUE THEN 'PUBLICADO'
    WHEN pendiente_revision = TRUE THEN 'PENDIENTE_REVISION'
    ELSE 'INACTIVO'
END
WHERE estado_publicacion IS NULL;

ALTER TABLE locales
    ALTER COLUMN estado_publicacion SET NOT NULL;

ALTER TABLE locales
    DROP CONSTRAINT IF EXISTS ck_locales_estado_publicacion;

ALTER TABLE locales
    ADD CONSTRAINT ck_locales_estado_publicacion
        CHECK (estado_publicacion IN (
            'PENDIENTE_REVISION',
            'PUBLICADO',
            'INACTIVO'
        ));

CREATE INDEX IF NOT EXISTS idx_locales_estado_publicacion
    ON locales (estado_publicacion, creado_en DESC);
