ALTER TABLE solicitudes_propietario
    ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(30);

-- Solo se clasifican automaticamente valores inequívocos de cedula ecuatoriana.
-- Los documentos historicos con otro formato permanecen sin tipo hasta revision.
UPDATE solicitudes_propietario
SET tipo_documento = 'CEDULA'
WHERE tipo_documento IS NULL
  AND identificacion ~ '^[0-9]{10}$';

ALTER TABLE solicitudes_propietario
    DROP CONSTRAINT IF EXISTS ck_solicitudes_tipo_documento;

ALTER TABLE solicitudes_propietario
    ADD CONSTRAINT ck_solicitudes_tipo_documento
        CHECK (tipo_documento IS NULL OR tipo_documento IN (
            'CEDULA',
            'PASAPORTE',
            'LICENCIA_CONDUCIR'
        ));
