package ec.edu.unl.lojavents.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public class ImporteReserva {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tarifa_servicio", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaServicio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    protected ImporteReserva() {
    }

    public ImporteReserva(BigDecimal subtotal, BigDecimal tarifaServicio, BigDecimal total) {
        this.subtotal = normalize(subtotal, "El subtotal no puede ser negativo.");
        this.tarifaServicio = normalize(tarifaServicio, "La tarifa de servicio no puede ser negativa.");
        this.total = normalize(total, "El total no puede ser negativo.");
        if (this.subtotal.add(this.tarifaServicio).compareTo(this.total) != 0) {
            throw new IllegalArgumentException("El total debe ser igual al subtotal mas la tarifa de servicio.");
        }
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

    private static BigDecimal normalize(BigDecimal value, String message) {
        BigDecimal required = Objects.requireNonNull(value, message);
        if (required.signum() < 0) {
            throw new IllegalArgumentException(message);
        }
        try {
            return required.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Los importes solo pueden tener dos decimales.", exception);
        }
    }
}
