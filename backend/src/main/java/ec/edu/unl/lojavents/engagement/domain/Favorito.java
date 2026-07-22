package ec.edu.unl.lojavents.engagement.domain;

import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "favoritos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorito_cliente_local",
                columnNames = {"cliente_id", "local_id"}
        )
)
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_id", nullable = false)
    private LocalEvento local;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    protected Favorito() {
    }

    public Favorito(Usuario cliente, LocalEvento local) {
        this.cliente = cliente;
        this.local = local;
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

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }
}
