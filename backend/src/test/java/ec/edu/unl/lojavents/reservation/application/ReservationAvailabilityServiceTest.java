package ec.edu.unl.lojavents.reservation.application;

import ec.edu.unl.lojavents.reservation.api.dto.AvailabilityResponse;
import ec.edu.unl.lojavents.reservation.domain.*;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationAvailabilityServiceTest {

    @Test
    void confirmedReservationBlocksAnOverlappingPeriod() {
        LocalEventoRepository venues = mock(LocalEventoRepository.class);
        ReservaRepository reservations = mock(ReservaRepository.class);
        ReservationAvailabilityService service = new ReservationAvailabilityService(venues, reservations);
        UUID venueId = UUID.randomUUID();
        LocalEvento venue = publishedVenue();
        LocalDate date = LocalDate.of(2030, 8, 20);
        Reserva confirmed = reservation(venue, date, LocalTime.of(16, 0), 3);
        confirmed.aprobarPago(new PagoSimulado(
                EstadoPagoSimulado.APROBADO,
                ModoPagoSimulado.APPROVE,
                "PAY-CONFIRMED",
                "Pago aprobado"
        ));

        when(venues.findById(venueId)).thenReturn(Optional.of(venue));
        when(reservations.findByVenueDatesAndStatus(isNull(), anyList(), eq(EstadoReserva.CONFIRMADA)))
                .thenReturn(List.of(confirmed));

        AvailabilityResponse result = service.check(venueId, date, LocalTime.of(17, 0), 1);

        assertFalse(result.available());
        assertTrue(result.message().contains("reservado"));
    }

    private LocalEvento publishedVenue() {
        LocalEvento venue = new LocalEvento(new Usuario("Owner", "owner@example.com", "hash", "0999999999"));
        venue.actualizar(
                "Venue", "Short", "Description", "Centro", "Main street",
                new BigDecimal("40.00"), 100,
                new LinkedHashSet<>(List.of("Bodas")),
                new LinkedHashSet<>(List.of("Wi-Fi")),
                List.of("Respect rules"), "No refunds", List.of("/image.jpg")
        );
        venue.solicitarRevision();
        venue.aprobarRevision();
        return venue;
    }

    private Reserva reservation(LocalEvento venue, LocalDate date, LocalTime time, int duration) {
        return new Reserva(
                new Usuario("Client", "client@example.com", "hash", "0988888888"),
                venue, date, time, duration, 20,
                "Loja", "Centro", "Main street",
                new BigDecimal("120.00"), new BigDecimal("9.60"), new BigDecimal("129.60"),
                true, true
        );
    }
}
