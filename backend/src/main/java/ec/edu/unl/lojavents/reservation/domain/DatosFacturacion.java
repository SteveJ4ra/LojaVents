package ec.edu.unl.lojavents.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DatosFacturacion {

    @Column(name = "ciudad_facturacion", nullable = false, length = 120)
    private String ciudad;

    @Column(name = "sector_facturacion", nullable = false, length = 120)
    private String sector;

    @Column(name = "direccion_facturacion", nullable = false, length = 300)
    private String direccion;

    protected DatosFacturacion() {
    }

    public DatosFacturacion(String ciudad, String sector, String direccion) {
        this.ciudad = requireText(ciudad, "La ciudad es obligatoria.", 120);
        this.sector = requireText(sector, "El sector es obligatorio.", 120);
        this.direccion = requireText(direccion, "La direccion es obligatoria.", 300);
    }

    public String getCiudad() {
        return ciudad;
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
