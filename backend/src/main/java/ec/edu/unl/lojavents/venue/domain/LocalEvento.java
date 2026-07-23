package ec.edu.unl.lojavents.venue.domain;

import ec.edu.unl.lojavents.user.domain.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "locales")
public class LocalEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(name = "descripcion_corta", nullable = false, length = 240)
    private String descripcionCorta;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Embedded
    private DireccionLocal direccionLocal;

    @Column(name = "precio_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioHora;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal calificacion = BigDecimal.ZERO;

    @Column(name = "total_resenas", nullable = false)
    private int totalResenas;

    @Column(nullable = false)
    private boolean destacado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private EstadoPublicacionLocal estadoPublicacion = EstadoPublicacionLocal.INACTIVO;

    @Column(name = "politica_cancelacion", nullable = false, columnDefinition = "text")
    private String politicaCancelacion;

    @ElementCollection
    @CollectionTable(name = "local_tipos_evento", joinColumns = @JoinColumn(name = "local_id"))
    @Column(name = "tipo", nullable = false, length = 80)
    private Set<String> tiposEvento = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "local_amenidades", joinColumns = @JoinColumn(name = "local_id"))
    @Column(name = "amenidad", nullable = false, length = 120)
    private Set<String> amenidades = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "local_reglas", joinColumns = @JoinColumn(name = "local_id"))
    @OrderColumn(name = "orden")
    @Column(name = "regla", nullable = false, columnDefinition = "text")
    private List<String> reglas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "local_imagenes", joinColumns = @JoinColumn(name = "local_id"))
    @OrderColumn(name = "orden")
    @Column(name = "url", nullable = false, length = 500)
    private List<String> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, horaInicio ASC")
    private List<BloqueDisponibilidad> bloqueos = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn = OffsetDateTime.now();

    protected LocalEvento() {
    }

    public LocalEvento(Usuario propietario) {
        this.propietario = propietario;
    }

    public void actualizar(
            String nombre,
            String descripcionCorta,
            String descripcion,
            String sector,
            String direccion,
            BigDecimal precioHora,
            int capacidad,
            Set<String> tiposEvento,
            Set<String> amenidades,
            List<String> reglas,
            String politicaCancelacion,
            List<String> imagenes
    ) {
        this.nombre = nombre;
        this.descripcionCorta = descripcionCorta;
        this.descripcion = descripcion;
        this.direccionLocal = new DireccionLocal(sector, direccion);
        this.precioHora = precioHora;
        this.capacidad = capacidad;
        this.tiposEvento.clear();
        this.tiposEvento.addAll(tiposEvento);
        this.amenidades.clear();
        this.amenidades.addAll(amenidades);
        this.reglas.clear();
        this.reglas.addAll(reglas);
        this.politicaCancelacion = politicaCancelacion;
        this.imagenes.clear();
        this.imagenes.addAll(imagenes);
        this.actualizadoEn = OffsetDateTime.now();
    }

    public void desactivar() {
        this.estadoPublicacion = EstadoPublicacionLocal.INACTIVO;
        this.actualizadoEn = OffsetDateTime.now();
    }

    public void solicitarRevision() {
        this.estadoPublicacion = EstadoPublicacionLocal.PENDIENTE_REVISION;
        this.actualizadoEn = OffsetDateTime.now();
    }

    public void aprobarRevision() {
        if (estadoPublicacion != EstadoPublicacionLocal.PENDIENTE_REVISION) {
            throw new IllegalStateException("Solo se puede publicar un local pendiente de revision.");
        }
        this.estadoPublicacion = EstadoPublicacionLocal.PUBLICADO;
        this.actualizadoEn = OffsetDateTime.now();
    }

    public void configurarDestacado(boolean destacado) {
        this.destacado = destacado;
    }

    public void configurarCalificacion(BigDecimal calificacion, int totalResenas) {
        this.calificacion = calificacion;
        this.totalResenas = totalResenas;
    }

    public void agregarBloqueo(BloqueDisponibilidad bloqueo) {
        bloqueo.asignarLocal(this);
        this.bloqueos.add(bloqueo);
        this.actualizadoEn = OffsetDateTime.now();
    }

    public boolean eliminarBloqueo(UUID bloqueoId) {
        boolean removed = bloqueos.removeIf(item -> item.getId().equals(bloqueoId));
        if (removed) {
            this.actualizadoEn = OffsetDateTime.now();
        }
        return removed;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getPropietario() {
        return propietario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSector() {
        return direccionLocal.getSector();
    }

    public String getDireccion() {
        return direccionLocal.getDireccion();
    }

    public DireccionLocal getDireccionLocal() {
        return direccionLocal;
    }

    public BigDecimal getPrecioHora() {
        return precioHora;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public BigDecimal getCalificacion() {
        return calificacion;
    }

    public int getTotalResenas() {
        return totalResenas;
    }

    public boolean isDestacado() {
        return destacado;
    }

    public boolean isActivo() {
        return estadoPublicacion == EstadoPublicacionLocal.PUBLICADO;
    }

    public boolean isPendienteRevision() {
        return estadoPublicacion == EstadoPublicacionLocal.PENDIENTE_REVISION;
    }

    public EstadoPublicacionLocal getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public String getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public Set<String> getTiposEvento() {
        return tiposEvento;
    }

    public Set<String> getAmenidades() {
        return amenidades;
    }

    public List<String> getReglas() {
        return reglas;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public List<BloqueDisponibilidad> getBloqueos() {
        return bloqueos;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public OffsetDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
