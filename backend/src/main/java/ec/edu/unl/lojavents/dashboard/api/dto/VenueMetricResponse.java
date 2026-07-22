package ec.edu.unl.lojavents.dashboard.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VenueMetricResponse(
        UUID venueId,
        String venueName,
        long reservations,
        long completedReservations,
        long rejectedReservations,
        BigDecimal revenue,
        BigDecimal rating
) {
}
