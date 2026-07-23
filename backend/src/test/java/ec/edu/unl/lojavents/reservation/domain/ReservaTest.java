package ec.edu.unl.lojavents.reservation.domain;

import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    @Test
    void approvedPaymentConfirmsReservationAndKeepsPublicReference() {
        Reserva reservation = reservation();
        String publicReference = reservation.getReferenciaPublica().getValor();

        reservation.aprobarPago(new PagoSimulado(
                EstadoPagoSimulado.APROBADO,
                ModoPagoSimulado.APPROVE,
                "PAY-001",
                "Pago aprobado"
        ));

        assertEquals(EstadoReserva.CONFIRMADA, reservation.getEstado());
        assertEquals(publicReference, reservation.getReferenciaPublica().getValor());
        assertEquals(EstadoPagoSimulado.APROBADO, reservation.getPago().getEstado());
    }

    static Reserva reservation() {
        Usuario client = new Usuario("Client", "client@example.com", "hash", "0988888888");
        Usuario owner = new Usuario("Owner", "owner@example.com", "hash", "0977777777");
        LocalEvento venue = new LocalEvento(owner);
        return new Reserva(
                client,
                venue,
                LocalDate.of(2026, 8, 20),
                LocalTime.of(16, 0),
                3,
                20,
                "Loja",
                "Centro",
                "Calle principal",
                new BigDecimal("120.00"),
                new BigDecimal("9.60"),
                new BigDecimal("129.60"),
                true,
                true
        );
    }
}
