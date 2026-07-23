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
        String publicReference,
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
                reserva.getReferenciaPublica().getValor(),
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
                pago == null ? null : publicPaymentReference(pago.getReferencia()),
                pago == null ? null : publicPaymentMessage(pago),
                publicRejectionReason(reserva.getMotivoRechazo()),
                reserva.getCreadoEn(),
                reserva.getActualizadoEn(),
                reserva.isResenaEnviada()
        );
    }

    static String publicPaymentReference(String reference) {
        if (reference == null) return null;
        return reference
                .replaceFirst("^SIM-OK-", "PAY-OK-")
                .replaceFirst("^SIM-REJ-", "PAY-REJ-")
                .replaceFirst("^SIM-DEMO-", "PAY-DEMO-")
                .replaceFirst("^SIM-", "PAY-");
    }

    static String publicPaymentMessage(PagoSimulado payment) {
        String message = payment.getMensaje();
        if (message == null || !message.toLowerCase().contains("simulad")) return message;
        return payment.getEstado().name().equals("APROBADO")
                ? "Pago aprobado correctamente."
                : "Pago rechazado.";
    }

    static String publicRejectionReason(String reason) {
        if (reason == null || !reason.toLowerCase().contains("simulad")) return reason;
        return "El pago fue rechazado.";
    }
}
