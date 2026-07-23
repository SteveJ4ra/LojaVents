package ec.edu.unl.lojavents.reservation.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoReservaTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 10);

    @Test
    void acceptsBoundaryDurations() {
        assertEquals(1, new PeriodoReserva(DATE, LocalTime.NOON, 1).getDuracionHoras());
        assertEquals(12, new PeriodoReserva(DATE, LocalTime.NOON, 12).getDuracionHoras());
    }

    @Test
    void rejectsDurationsOutsideOneToTwelveHours() {
        assertThrows(IllegalArgumentException.class,
                () -> new PeriodoReserva(DATE, LocalTime.NOON, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new PeriodoReserva(DATE, LocalTime.NOON, 13));
    }

    @Test
    void calculatesEndAndDetectsOverlapAcrossMidnight() {
        PeriodoReserva first = new PeriodoReserva(DATE, LocalTime.of(22, 0), 4);
        PeriodoReserva overlapping = new PeriodoReserva(DATE.plusDays(1), LocalTime.of(1, 0), 2);
        PeriodoReserva adjacent = new PeriodoReserva(DATE.plusDays(1), LocalTime.of(2, 0), 2);

        assertEquals(DATE.plusDays(1).atTime(2, 0), first.fin());
        assertTrue(first.seSolapaCon(overlapping));
        assertFalse(first.seSolapaCon(adjacent));
    }
}
