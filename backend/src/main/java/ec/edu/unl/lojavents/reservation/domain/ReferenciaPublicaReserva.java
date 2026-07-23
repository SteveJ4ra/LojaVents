package ec.edu.unl.lojavents.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ReferenciaPublicaReserva {

    private static final String PREFIX = "LV-";

    @Column(name = "referencia_publica", nullable = false, length = 40, unique = true)
    private String valor;

    protected ReferenciaPublicaReserva() {
    }

    private ReferenciaPublicaReserva(String valor) {
        String normalized = Objects.requireNonNull(valor, "La referencia publica es obligatoria.").trim();
        if (!normalized.matches("LV-[A-F0-9]{32}")) {
            throw new IllegalArgumentException("La referencia publica no tiene un formato valido.");
        }
        this.valor = normalized;
    }

    public static ReferenciaPublicaReserva generar() {
        String random = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return new ReferenciaPublicaReserva(PREFIX + random);
    }

    public static ReferenciaPublicaReserva desde(String valor) {
        return new ReferenciaPublicaReserva(valor);
    }

    public String getValor() {
        return valor;
    }
}
