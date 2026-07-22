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

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "duracion_horas", nullable = false)
    private int duracionHoras;

    @Column(nullable = false)
    private int asistentes;

    @Column(name = "ciudad_facturacion", nullable = false, length = 120)
    private String ciudadFacturacion;

    @Column(name = "sector_facturacion", nullable = false, length = 120)
    private String sectorFacturacion;

    @Column(name = "direccion_facturacion", nullable = false, length = 300)
    private String direccionFacturacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tarifa_servicio", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaServicio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

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
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracionHoras = duracionHoras;
        this.asistentes = asistentes;
        this.ciudadFacturacion = ciudadFacturacion;
        this.sectorFacturacion = sectorFacturacion;
        this.direccionFacturacion = direccionFacturacion;
        this.subtotal = subtotal;
        this.tarifaServicio = tarifaServicio;
        this.total = total;
        this.reglasAceptadas = reglasAceptadas;
        this.cancelacionAceptada = cancelacionAceptada;
    }

    public void aprobarPago(PagoSimulado pago) {
        asignarPago(pago);
        this.estado = EstadoReserva.COMPLETADA;
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
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public int getDuracionHoras() {
        return duracionHoras;
    }

    public int getAsistentes() {
        return asistentes;
    }

    public String getCiudadFacturacion() {
        return ciudadFacturacion;
    }

    public String getSectorFacturacion() {
        return sectorFacturacion;
    }

    public String getDireccionFacturacion() {
        return direccionFacturacion;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTarifaServicio() {
        return tarifaServicio;
    }

    public BigDecimal getTotal() {
        return total;
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
