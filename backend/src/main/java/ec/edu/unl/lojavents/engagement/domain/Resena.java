package ec.edu.unl.lojavents.engagement.domain;

import ec.edu.unl.lojavents.reservation.domain.Reserva;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_id", nullable = false)
    private LocalEvento local;

    @Column(nullable = false)
    private int calificacion;

    @Column(nullable = false, columnDefinition = "text")
    private String comentario;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn = OffsetDateTime.now();

    protected Resena() {
    }

    public Resena(Reserva reserva, int calificacion, String comentario) {
        this.reserva = reserva;
        this.cliente = reserva.getCliente();
        this.local = reserva.getLocal();
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public UUID getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public LocalEvento getLocal() {
        return local;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public OffsetDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
