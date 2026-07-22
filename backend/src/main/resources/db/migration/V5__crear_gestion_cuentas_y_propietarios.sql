ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

UPDATE usuarios
SET estado = CASE WHEN activo THEN 'ACTIVO' ELSE 'INACTIVO' END;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS estado_verificacion_propietario VARCHAR(24)
        NOT NULL DEFAULT 'NO_SOLICITADA';

UPDATE usuarios u
SET estado_verificacion_propietario = 'APROBADA'
WHERE EXISTS (
    SELECT 1
    FROM usuario_roles ur
    WHERE ur.usuario_id = u.id
      AND ur.rol = 'PROPIETARIO'
);

CREATE TABLE IF NOT EXISTS solicitudes_propietario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    identificacion VARCHAR(30) NOT NULL,
    documento_referencia VARCHAR(255) NOT NULL,
    notas VARCHAR(1200) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    revisado_por UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    comentario_admin VARCHAR(600),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revisado_en TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_solicitudes_propietario_usuario
    ON solicitudes_propietario (usuario_id, creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_solicitudes_propietario_estado
    ON solicitudes_propietario (estado, creado_en ASC);

CREATE UNIQUE INDEX IF NOT EXISTS uq_solicitud_propietario_pendiente
    ON solicitudes_propietario (usuario_id)
    WHERE estado = 'PENDIENTE';
