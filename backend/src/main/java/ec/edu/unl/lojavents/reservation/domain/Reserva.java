package ec.edu.unl.lojavents.reservation.domain;

import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_id", nullable = false)
    private LocalEvento local;

    @Embedded
    private PeriodoReserva periodo;

    @Column(nullable = false)
    private int asistentes;

    @Embedded
    private DatosFacturacion datosFacturacion;

    @Embedded
    private ImporteReserva importe;

    @Embedded
    private ReferenciaPublicaReserva referenciaPublica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReserva estado = EstadoReserva.EN_PROCESO;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @Column(name = "reglas_aceptadas", nullable = false)
    private boolean reglasAceptadas;

    @Column(name = "cancelacion_aceptada", nullable = false)
    private boolean cancelacionAceptada;

    @Column(name = "resena_enviada", nullable = false)
    private boolean resenaEnviada;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PagoSimulado pago;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn = OffsetDateTime.now();

    @Version
    @Column(nullable = false)
    private long version;

    protected Reserva() {
    }

    public Reserva(
            Usuario cliente,
            LocalEvento local,
            LocalDate fecha,
            LocalTime horaInicio,
            int duracionHoras,
            int asistentes,
            String ciudadFacturacion,
            String sectorFacturacion,
            String direccionFacturacion,
            BigDecimal subtotal,
            BigDecimal tarifaServicio,
            BigDecimal total,
            boolean reglasAceptadas,
            boolean cancelacionAceptada
    ) {
        this.cliente = cliente;
        this.local = local;
        this.periodo = new PeriodoReserva(fecha, horaInicio, duracionHoras);
        this.asistentes = asistentes;
        this.datosFacturacion = new DatosFacturacion(
                ciudadFacturacion,
                sectorFacturacion,
                direccionFacturacion
        );
        this.importe = new ImporteReserva(subtotal, tarifaServicio, total);
        this.referenciaPublica = ReferenciaPublicaReserva.generar();
        this.reglasAceptadas = reglasAceptadas;
        this.cancelacionAceptada = cancelacionAceptada;
    }

    public void aprobarPago(PagoSimulado pago) {
        asignarPago(pago);
        this.estado = EstadoReserva.CONFIRMADA;
        this.motivoRechazo = null;
        this.actualizadoEn = OffsetDateTime.now();
    }

    public void rechazarPago(PagoSimulado pago, String motivo) {
        asignarPago(pago);
        this.estado = EstadoReserva.RECHAZADA;
        this.motivoRechazo = motivo;
        this.actualizadoEn = OffsetDateTime.now();
    }

    private void asignarPago(PagoSimulado pago) {
        pago.asignarReserva(this);
        this.pago = pago;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public LocalEvento getLocal() {
        return local;
    }

    public LocalDate getFecha() {
        return periodo.getFecha();
    }

    public LocalTime getHoraInicio() {
        return periodo.getHoraInicio();
    }

    public int getDuracionHoras() {
        return periodo.getDuracionHoras();
    }

    public PeriodoReserva getPeriodo() {
        return periodo;
    }

    public int getAsistentes() {
        return asistentes;
    }

    public String getCiudadFacturacion() {
        return datosFacturacion.getCiudad();
    }

    public String getSectorFacturacion() {
        return datosFacturacion.getSector();
    }

    public String getDireccionFacturacion() {
        return datosFacturacion.getDireccion();
    }

    public DatosFacturacion getDatosFacturacion() {
        return datosFacturacion;
    }

    public BigDecimal getSubtotal() {
        return importe.getSubtotal();
    }

    public BigDecimal getTarifaServicio() {
        return importe.getTarifaServicio();
    }

    public BigDecimal getTotal() {
        return importe.getTotal();
    }

    public ImporteReserva getImporte() {
        return importe;
    }

    public ReferenciaPublicaReserva getReferenciaPublica() {
        return referenciaPublica;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public boolean isReglasAceptadas() {
        return reglasAceptadas;
    }

    public boolean isCancelacionAceptada() {
        return cancelacionAceptada;
    }

    public boolean isResenaEnviada() {
        return resenaEnviada;
    }

    public void marcarResenaEnviada() {
        this.resenaEnviada = true;
        this.actualizadoEn = OffsetDateTime.now();
    }

    public PagoSimulado getPago() {
        return pago;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public OffsetDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
