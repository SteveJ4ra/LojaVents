CREATE TABLE IF NOT EXISTS reservas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL REFERENCES usuarios(id),
    local_id UUID NOT NULL REFERENCES locales(id),
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    duracion_horas INTEGER NOT NULL CHECK (duracion_horas BETWEEN 1 AND 12),
    asistentes INTEGER NOT NULL CHECK (asistentes > 0),
    ciudad_facturacion VARCHAR(120) NOT NULL,
    sector_facturacion VARCHAR(120) NOT NULL,
    direccion_facturacion VARCHAR(300) NOT NULL,
    subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    tarifa_servicio NUMERIC(10,2) NOT NULL CHECK (tarifa_servicio >= 0),
    total NUMERIC(10,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(30) NOT NULL,
    motivo_rechazo VARCHAR(500),
    reglas_aceptadas BOOLEAN NOT NULL,
    cancelacion_aceptada BOOLEAN NOT NULL,
    resena_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_reservas_cliente
    ON reservas(cliente_id, creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_reservas_local_fecha
    ON reservas(local_id, fecha, estado);

CREATE TABLE IF NOT EXISTS pagos_simulados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reserva_id UUID NOT NULL UNIQUE REFERENCES reservas(id) ON DELETE CASCADE,
    estado VARCHAR(30) NOT NULL,
    modo VARCHAR(40) NOT NULL,
    referencia VARCHAR(90) NOT NULL UNIQUE,
    mensaje VARCHAR(500) NOT NULL,
    procesado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
