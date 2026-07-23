package ec.edu.unl.lojavents.user.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_propietario")
public class SolicitudPropietario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Embedded
    private DocumentoIdentidad documentoIdentidad;

    @Column(name = "documento_referencia", nullable = false, length = 255)
    private String documentoReferencia;

    @Column(name = "documento_archivo_id", length = 80)
    private String documentoArchivoId;

    @Column(name = "documento_nombre", length = 255)
    private String documentoNombre;

    @Column(name = "documento_tipo", length = 120)
    private String documentoTipo;

    @Column(name = "documento_tamano")
    private Long documentoTamano;

    @Column(nullable = false, length = 1200)
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudPropietario estado = EstadoSolicitudPropietario.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por")
    private Usuario revisadoPor;

    @Column(name = "comentario_admin", length = 600)
    private String comentarioAdmin;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @Column(name = "revisado_en")
    private OffsetDateTime revisadoEn;

    protected SolicitudPropietario() {
    }

    public SolicitudPropietario(
            Usuario usuario,
            TipoDocumentoIdentidad tipoDocumento,
            String identificacion,
            String documentoReferencia,
            String documentoArchivoId,
            String documentoTipo,
            long documentoTamano,
            String notas
    ) {
        this.usuario = usuario;
        this.documentoIdentidad = new DocumentoIdentidad(tipoDocumento, identificacion);
        this.documentoReferencia = documentoReferencia;
        this.documentoArchivoId = documentoArchivoId;
        this.documentoNombre = documentoReferencia;
        this.documentoTipo = documentoTipo;
        this.documentoTamano = documentoTamano;
        this.notas = notas;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getIdentificacion() {
        return documentoIdentidad.getNumero();
    }

    public TipoDocumentoIdentidad getTipoDocumento() {
        return documentoIdentidad.getTipo();
    }

    public DocumentoIdentidad getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public String getDocumentoReferencia() {
        return documentoReferencia;
    }

    public String getDocumentoArchivoId() {
        return documentoArchivoId;
    }

    public String getDocumentoNombre() {
        return documentoNombre;
    }

    public String getDocumentoTipo() {
        return documentoTipo;
    }

    public Long getDocumentoTamano() {
        return documentoTamano;
    }

    public String getNotas() {
        return notas;
    }

    public EstadoSolicitudPropietario getEstado() {
        return estado;
    }

    public Usuario getRevisadoPor() {
        return revisadoPor;
    }

    public String getComentarioAdmin() {
        return comentarioAdmin;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public OffsetDateTime getRevisadoEn() {
        return revisadoEn;
    }

    public void aprobar(Usuario admin, String comentario) {
        estado = EstadoSolicitudPropietario.APROBADA;
        revisadoPor = admin;
        comentarioAdmin = normalizarComentario(comentario);
        revisadoEn = OffsetDateTime.now();
    }

    public void rechazar(Usuario admin, String comentario) {
        estado = EstadoSolicitudPropietario.RECHAZADA;
        revisadoPor = admin;
        comentarioAdmin = normalizarComentario(comentario);
        revisadoEn = OffsetDateTime.now();
    }

    private String normalizarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        return comentario.trim();
    }
}
