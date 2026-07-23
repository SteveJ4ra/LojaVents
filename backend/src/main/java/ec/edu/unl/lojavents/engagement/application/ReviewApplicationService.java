package ec.edu.unl.lojavents.engagement.application;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.engagement.api.dto.ReviewRequest;
import ec.edu.unl.lojavents.engagement.api.dto.ReviewResponse;
import ec.edu.unl.lojavents.engagement.domain.Resena;
import ec.edu.unl.lojavents.engagement.repository.ResenaRepository;
import ec.edu.unl.lojavents.reservation.domain.EstadoReserva;
import ec.edu.unl.lojavents.reservation.domain.Reserva;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewApplicationService {

    private static final ZoneId LOJA_ZONE = ZoneId.of("America/Guayaquil");

    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final LocalEventoRepository localRepository;
    private final AuditEventRepository auditRepository;

    public ReviewApplicationService(
            ResenaRepository resenaRepository,
            ReservaRepository reservaRepository,
            LocalEventoRepository localRepository,
            AuditEventRepository auditRepository
    ) {
        this.resenaRepository = resenaRepository;
        this.reservaRepository = reservaRepository;
        this.localRepository = localRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> byVenue(UUID venueId) {
        if (!localRepository.existsById(venueId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND", "El local no existe.");
        }
        return resenaRepository.findByLocal_IdOrderByCreadoEnDesc(venueId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse create(
            String subject,
            UUID reservationId,
            ReviewRequest request
    ) {
        UUID userId = parseSubject(subject);
        Reserva reservation = reservaRepository.findOwnedByClient(reservationId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        "La reserva no existe o no pertenece al usuario autenticado."
                ));

        if (reservation.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "RESERVATION_NOT_COMPLETED",
                    "Solo se puede reseñar una reserva confirmada."
            );
        }

        LocalDate today = LocalDate.now(LOJA_ZONE);
        if (!reservation.getFecha().isBefore(today)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "EVENT_NOT_FINISHED",
                    "Podrás publicar la reseña después de la fecha del evento."
            );
        }

        if (reservation.isResenaEnviada()
                || resenaRepository.existsByReserva_Id(reservationId)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "REVIEW_ALREADY_EXISTS",
                    "Esta reserva ya tiene una reseña."
            );
        }

        Resena review = new Resena(
                reservation,
                request.rating(),
                request.comment().trim()
        );
        resenaRepository.saveAndFlush(review);
        reservation.marcarResenaEnviada();
        reservaRepository.save(reservation);

        LocalEvento venue = reservation.getLocal();
        recalculateRating(venue);

        auditRepository.save(new AuditEvent(
                "RESENA_PUBLICADA",
                userId.toString(),
                "El usuario publicó una reseña verificada.",
                Map.of(
                        "reservationId", reservationId.toString(),
                        "venueId", venue.getId().toString(),
                        "rating", request.rating()
                )
        ));
        return ReviewResponse.from(review);
    }

    private void recalculateRating(LocalEvento venue) {
        List<Resena> reviews = resenaRepository.findByLocal_IdOrderByCreadoEnDesc(venue.getId());
        BigDecimal average = reviews.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(
                        reviews.stream().mapToInt(Resena::getCalificacion).average().orElse(0)
                ).setScale(1, RoundingMode.HALF_UP);

        venue.configurarCalificacion(average, reviews.size());
        localRepository.save(venue);
    }

    private UUID parseSubject(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido.");
        }
    }
}
