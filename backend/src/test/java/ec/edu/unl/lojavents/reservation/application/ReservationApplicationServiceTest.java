package ec.edu.unl.lojavents.reservation.application;

import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.reservation.api.dto.PaymentSimulationRequest;
import ec.edu.unl.lojavents.reservation.domain.ModoPagoSimulado;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationApplicationServiceTest {

    @Test
    void deniesPaymentAccessToAReservationOwnedByAnotherUser() {
        ReservaRepository reservations = mock(ReservaRepository.class);
        ReservationApplicationService service = new ReservationApplicationService(
                reservations,
                mock(UsuarioRepository.class),
                mock(LocalEventoRepository.class),
                mock(ReservationAvailabilityService.class),
                mock(AuditEventRepository.class)
        );
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        when(reservations.findOwnedByClient(reservationId, userId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> service.simulatePayment(
                userId.toString(),
                reservationId,
                new PaymentSimulationRequest(ModoPagoSimulado.APPROVE)
        ));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("RESERVATION_NOT_FOUND", exception.getCode());
    }
}
