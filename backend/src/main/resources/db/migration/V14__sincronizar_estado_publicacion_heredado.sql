CREATE OR REPLACE FUNCTION sincronizar_estado_publicacion_heredado()
RETURNS TRIGGER AS $$
BEGIN
    NEW.activo = NEW.estado_publicacion = 'PUBLICADO';
    NEW.pendiente_revision = NEW.estado_publicacion = 'PENDIENTE_REVISION';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_locales_estado_publicacion_heredado ON locales;

CREATE TRIGGER trg_locales_estado_publicacion_heredado
BEFORE INSERT OR UPDATE OF estado_publicacion ON locales
FOR EACH ROW
EXECUTE FUNCTION sincronizar_estado_publicacion_heredado();

UPDATE locales
SET activo = estado_publicacion = 'PUBLICADO',
    pendiente_revision = estado_publicacion = 'PENDIENTE_REVISION';
