CREATE TABLE IF NOT EXISTS favoritos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_favorito_cliente_local UNIQUE (cliente_id, local_id)
);

CREATE INDEX IF NOT EXISTS idx_favoritos_cliente
    ON favoritos(cliente_id, creado_en DESC);

CREATE TABLE IF NOT EXISTS resenas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reserva_id UUID NOT NULL UNIQUE REFERENCES reservas(id) ON DELETE CASCADE,
    cliente_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    calificacion INTEGER NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
    comentario TEXT NOT NULL CHECK (char_length(trim(comentario)) >= 10),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_resenas_local
    ON resenas(local_id, creado_en DESC);

-- Las calificaciones dejan de ser datos simulados y pasan a calcularse
-- exclusivamente a partir de las reseñas persistidas.
UPDATE locales
SET calificacion = 0,
    total_resenas = 0;
