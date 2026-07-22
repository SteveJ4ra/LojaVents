package ec.edu.unl.lojavents.reservation.api.dto;

import ec.edu.unl.lojavents.reservation.domain.PagoSimulado;
import ec.edu.unl.lojavents.reservation.domain.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        UUID venueId,
        String venueName,
        LocalDate date,
        LocalTime startTime,
        int durationHours,
        int attendees,
        BillingAddressRequest billingAddress,
        BigDecimal subtotal,
        BigDecimal serviceFee,
        BigDecimal total,
        String status,
        String paymentStatus,
        String paymentMode,
        String paymentReference,
        String paymentMessage,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean reviewSubmitted
) {
    public static ReservationResponse from(Reserva reserva) {
        PagoSimulado pago = reserva.getPago();
        return new ReservationResponse(
                reserva.getId(),
                reserva.getCliente().getId(),
                reserva.getCliente().getNombres(),
                reserva.getCliente().getEmail(),
                reserva.getLocal().getId(),
                reserva.getLocal().getNombre(),
                reserva.getFecha(),
                reserva.getHoraInicio(),
                reserva.getDuracionHoras(),
                reserva.getAsistentes(),
                new BillingAddressRequest(
                        reserva.getCiudadFacturacion(),
                        reserva.getSectorFacturacion(),
                        reserva.getDireccionFacturacion()
                ),
                reserva.getSubtotal(),
                reserva.getTarifaServicio(),
                reserva.getTotal(),
                reserva.getEstado().name(),
                pago == null ? null : pago.getEstado().name(),
                pago == null ? null : pago.getModo().name(),
                pago == null ? null : pago.getReferencia(),
                pago == null ? null : pago.getMensaje(),
                reserva.getMotivoRechazo(),
                reserva.getCreadoEn(),
                reserva.getActualizadoEn(),
                reserva.isResenaEnviada()
        );
    }
}
