package ec.edu.unl.lojavents.reservation.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ImporteReservaTest {

    @Test
    void acceptsCoherentAmountsAndNormalizesScale() {
        ImporteReserva amount = new ImporteReserva(
                new BigDecimal("100"),
                new BigDecimal("8.0"),
                new BigDecimal("108.00")
        );

        assertEquals(new BigDecimal("100.00"), amount.getSubtotal());
        assertEquals(new BigDecimal("8.00"), amount.getTarifaServicio());
        assertEquals(new BigDecimal("108.00"), amount.getTotal());
    }

    @Test
    void rejectsNegativeIncoherentOrOverPreciseAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new ImporteReserva(
                new BigDecimal("-1"), BigDecimal.ZERO, new BigDecimal("-1")
        ));
        assertThrows(IllegalArgumentException.class, () -> new ImporteReserva(
                new BigDecimal("100"), new BigDecimal("8"), new BigDecimal("109")
        ));
        assertThrows(IllegalArgumentException.class, () -> new ImporteReserva(
                new BigDecimal("100.001"), new BigDecimal("8"), new BigDecimal("108.001")
        ));
    }
}
