package ec.edu.unl.lojavents.venue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DireccionLocal {

    @Column(nullable = false, length = 120)
    private String sector;

    @Column(nullable = false, length = 240)
    private String direccion;

    protected DireccionLocal() {
    }

    public DireccionLocal(String sector, String direccion) {
        this.sector = requireText(sector, "El sector es obligatorio.", 120);
        this.direccion = requireText(direccion, "La direccion es obligatoria.", 240);
    }

    public String getSector() {
        return sector;
    }

    public String getDireccion() {
        return direccion;
    }

    private static String requireText(String value, String message, int maxLength) {
        String normalized = Objects.requireNonNull(value, message).trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
