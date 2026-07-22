CREATE TABLE locales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    propietario_id UUID NOT NULL REFERENCES usuarios(id),
    nombre VARCHAR(160) NOT NULL,
    descripcion_corta VARCHAR(240) NOT NULL,
    descripcion TEXT NOT NULL,
    sector VARCHAR(120) NOT NULL,
    direccion VARCHAR(240) NOT NULL,
    precio_hora NUMERIC(10,2) NOT NULL CHECK (precio_hora > 0),
    capacidad INTEGER NOT NULL CHECK (capacidad > 0),
    calificacion NUMERIC(2,1) NOT NULL DEFAULT 0,
    total_resenas INTEGER NOT NULL DEFAULT 0,
    destacado BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    politica_cancelacion TEXT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_locales_propietario ON locales(propietario_id);
CREATE INDEX idx_locales_activo_destacado ON locales(activo, destacado);

CREATE TABLE local_tipos_evento (
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    tipo VARCHAR(80) NOT NULL,
    PRIMARY KEY (local_id, tipo)
);

CREATE TABLE local_amenidades (
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    amenidad VARCHAR(120) NOT NULL,
    PRIMARY KEY (local_id, amenidad)
);

CREATE TABLE local_reglas (
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    orden INTEGER NOT NULL,
    regla TEXT NOT NULL,
    PRIMARY KEY (local_id, orden)
);

CREATE TABLE local_imagenes (
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    orden INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (local_id, orden)
);

CREATE TABLE local_bloqueos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    local_id UUID NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo VARCHAR(180) NOT NULL,
    CHECK (hora_inicio < hora_fin)
);

CREATE INDEX idx_local_bloqueos_fecha ON local_bloqueos(local_id, fecha);
