CREATE INDEX IF NOT EXISTS idx_usuarios_estado
    ON usuarios (estado);

CREATE INDEX IF NOT EXISTS idx_locales_activo
    ON locales (activo);

CREATE INDEX IF NOT EXISTS idx_reservas_estado_creado
    ON reservas (estado, creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_reservas_fecha_estado
    ON reservas (fecha, estado);

CREATE INDEX IF NOT EXISTS idx_reservas_local_estado
    ON reservas (local_id, estado, creado_en DESC);
