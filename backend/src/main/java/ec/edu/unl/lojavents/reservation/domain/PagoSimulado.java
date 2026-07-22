package ec.edu.unl.lojavents.reservation.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos_simulados")
public class PagoSimulado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPagoSimulado estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ModoPagoSimulado modo;

    @Column(nullable = false, unique = true, length = 90)
    private String referencia;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "procesado_en", nullable = false)
    private OffsetDateTime procesadoEn = OffsetDateTime.now();

    protected PagoSimulado() {
    }

    public PagoSimulado(
            EstadoPagoSimulado estado,
            ModoPagoSimulado modo,
            String referencia,
            String mensaje
    ) {
        this.estado = estado;
        this.modo = modo;
        this.referencia = referencia;
        this.mensaje = mensaje;
    }

    void asignarReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public UUID getId() {
        return id;
    }

    public EstadoPagoSimulado getEstado() {
        return estado;
    }

    public ModoPagoSimulado getModo() {
        return modo;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getMensaje() {
        return mensaje;
    }

    public OffsetDateTime getProcesadoEn() {
        return procesadoEn;
    }
}
