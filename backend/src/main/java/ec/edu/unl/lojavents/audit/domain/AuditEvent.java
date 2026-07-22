package ec.edu.unl.lojavents.audit.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_events")
public class AuditEvent {

    @Id
    private String id;

    private String tipo;
    private String actor;
    private String mensaje;
    private Map<String, Object> datos;
    private Instant creadoEn = Instant.now();

    protected AuditEvent() {
    }

    public AuditEvent(String tipo, String actor, String mensaje, Map<String, Object> datos) {
        this.tipo = tipo;
        this.actor = actor;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public String getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getActor() {
        return actor;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
