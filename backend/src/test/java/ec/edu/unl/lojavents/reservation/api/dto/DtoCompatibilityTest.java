package ec.edu.unl.lojavents.reservation.api.dto;

import ec.edu.unl.lojavents.reservation.domain.*;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.venue.api.dto.VenueResponse;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DtoCompatibilityTest {

    @Test
    void keepsLegacyVenueFlagsAndReservationFields() {
        Usuario owner = new Usuario("Owner", "owner@example.com", "hash", "0999999999");
        LocalEvento venue = new LocalEvento(owner);
        venue.actualizar(
                "Venue", "Short", "Description", "Centro", "Main street",
                new BigDecimal("40.00"), 100,
                new LinkedHashSet<>(List.of("Bodas")),
                new LinkedHashSet<>(List.of("Wi-Fi")),
                List.of("Respect rules"), "No refunds", List.of("/image.jpg")
        );
        venue.solicitarRevision();
        VenueResponse pending = VenueResponse.from(venue);
        assertFalse(pending.active());
        assertTrue(pending.pendingReview());

        Reserva reservation = new Reserva(
                new Usuario("Client", "client@example.com", "hash", "0988888888"),
                venue, LocalDate.of(2030, 8, 20), LocalTime.of(16, 0), 3, 20,
                "Loja", "Centro", "Main street",
                new BigDecimal("120.00"), new BigDecimal("9.60"), new BigDecimal("129.60"),
                true, true
        );
        reservation.aprobarPago(new PagoSimulado(
                EstadoPagoSimulado.APROBADO, ModoPagoSimulado.APPROVE,
                "PAY-DTO", "Pago aprobado"
        ));
        ReservationResponse response = ReservationResponse.from(reservation);

        assertEquals("CONFIRMADA", response.status());
        assertEquals(3, response.durationHours());
        assertEquals("Centro", response.billingAddress().neighborhood());
        assertTrue(response.publicReference().startsWith("LV-"));
    }
}
