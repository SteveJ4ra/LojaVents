package ec.edu.unl.lojavents.engagement.api.dto;

import ec.edu.unl.lojavents.engagement.domain.Resena;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        UUID venueId,
        UUID userId,
        String userName,
        int rating,
        String comment,
        OffsetDateTime createdAt
) {
    public static ReviewResponse from(Resena review) {
        return new ReviewResponse(
                review.getId(),
                review.getReserva().getId(),
                review.getLocal().getId(),
                review.getCliente().getId(),
                review.getCliente().getNombres(),
                review.getCalificacion(),
                review.getComentario(),
                review.getCreadoEn()
        );
    }
}
