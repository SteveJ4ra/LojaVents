package ec.edu.unl.lojavents.reservation.application;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.reservation.api.dto.CreateReservationRequest;
import ec.edu.unl.lojavents.reservation.api.dto.PaymentSimulationRequest;
import ec.edu.unl.lojavents.reservation.api.dto.ReservationResponse;
import ec.edu.unl.lojavents.reservation.domain.*;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReservationApplicationService {

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.08");

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalEventoRepository localRepository;
    private final ReservationAvailabilityService availabilityService;
    private final AuditEventRepository auditRepository;

    public ReservationApplicationService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            LocalEventoRepository localRepository,
            ReservationAvailabilityService availabilityService,
            AuditEventRepository auditRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.localRepository = localRepository;
        this.availabilityService = availabilityService;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public ReservationResponse createDraft(String subject, CreateReservationRequest request) {
        UUID userId = parseSubject(subject);
        Usuario client = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "USER_NOT_FOUND",
                        "El usuario de la sesión ya no existe."
                ));
        if (!client.isActivo()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", "La cuenta no está activa.");
        }

        LocalEvento venue = localRepository.findById(request.venueId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "VENUE_NOT_FOUND",
                        "El local no existe."
                ));
        if (request.attendees() > venue.getCapacidad()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "CAPACITY_EXCEEDED",
                    "La cantidad de asistentes supera la capacidad del local."
            );
        }

        availabilityService.assertAvailable(
                venue,
                request.date(),
                request.startTime(),
                request.durationHours()
        );

        BigDecimal subtotal = venue.getPrecioHora()
                .multiply(BigDecimal.valueOf(request.durationHours()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(serviceFee).setScale(2, RoundingMode.HALF_UP);

        Reserva reservation = new Reserva(
                client,
                venue,
                request.date(),
                request.startTime(),
                request.durationHours(),
                request.attendees(),
                request.billingAddress().city().trim(),
                request.billingAddress().neighborhood().trim(),
                request.billingAddress().street().trim(),
                subtotal,
                serviceFee,
                total,
                request.acceptedRules(),
                request.acceptedCancellation()
        );
        reservaRepository.save(reservation);

        audit(
                "RESERVA_INICIADA",
                userId,
                reservation,
                Map.of("status", reservation.getEstado().name())
        );
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse simulatePayment(
            String subject,
            UUID reservationId,
            PaymentSimulationRequest request
    ) {
        UUID userId = parseSubject(subject);
        Reserva reservation = reservaRepository.findOwnedByClient(reservationId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        "La reserva no existe o no pertenece al usuario autenticado."
                ));

        if (reservation.getEstado() != EstadoReserva.EN_PROCESO) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "RESERVATION_ALREADY_PROCESSED",
                    "La reserva ya tiene un resultado de pago."
            );
        }

        PaymentOutcome outcome = outcomeFor(request.mode());
        String reference = createReference(outcome.approved());
        PagoSimulado payment = new PagoSimulado(
                outcome.approved() ? EstadoPagoSimulado.APROBADO : EstadoPagoSimulado.RECHAZADO,
                request.mode(),
                reference,
                outcome.message()
        );

        if (outcome.approved()) {
            LocalEvento lockedVenue = localRepository.findByIdForUpdate(reservation.getLocal().getId())
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "VENUE_NOT_FOUND",
                            "El local ya no existe."
                    ));
            availabilityService.assertAvailable(
                    lockedVenue,
                    reservation.getFecha(),
                    reservation.getHoraInicio(),
                    reservation.getDuracionHoras()
            );
            reservation.aprobarPago(payment);
        } else {
            reservation.rechazarPago(payment, outcome.message());
        }

        reservaRepository.save(reservation);
        audit(
                outcome.approved() ? "PAGO_SIMULADO_APROBADO" : "PAGO_SIMULADO_RECHAZADO",
                userId,
                reservation,
                Map.of(
                        "paymentMode", request.mode().name(),
                        "paymentReference", reference,
                        "status", reservation.getEstado().name()
                )
        );
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations(String subject) {
        UUID userId = parseSubject(subject);
        return reservaRepository.findMine(userId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> ownerReservations(String subject) {
        UUID ownerId = parseSubject(subject);
        return reservaRepository.findForOwner(ownerId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private PaymentOutcome outcomeFor(ModoPagoSimulado mode) {
        return switch (mode) {
            case APPROVE -> new PaymentOutcome(true, "Pago aprobado correctamente.");
            case REJECT_FUNDS -> new PaymentOutcome(false, "Pago rechazado por fondos insuficientes.");
            case REJECT_PROVIDER -> new PaymentOutcome(false, "Pago rechazado por indisponibilidad del proveedor.");
            case REJECT_APPLICATION -> new PaymentOutcome(false, "Pago rechazado por un error de la aplicación.");
        };
    }

    private String createReference(boolean approved) {
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "%s-%s-%s".formatted(approved ? "PAY-OK" : "PAY-REJ", timestamp, suffix);
    }

    private UUID parseSubject(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido.");
        }
    }

    private void audit(
            String type,
            UUID actor,
            Reserva reservation,
            Map<String, Object> data
    ) {
        Map<String, Object> details = new LinkedHashMap<>(data);
        details.put("reservationId", reservation.getId().toString());
        details.put("venueId", reservation.getLocal().getId().toString());
        auditRepository.save(new AuditEvent(
                type,
                actor.toString(),
                "Operación realizada sobre una reserva.",
                details
        ));
    }

    private record PaymentOutcome(boolean approved, String message) {
    }
}
