package ec.edu.unl.lojavents.dashboard.api.dto;

import ec.edu.unl.lojavents.reservation.api.dto.ReservationResponse;

import java.math.BigDecimal;
import java.util.List;

public record OwnerDashboardResponse(
        long totalVenues,
        long activeVenues,
        long totalReservations,
        long completedReservations,
        long rejectedReservations,
        long upcomingReservations,
        BigDecimal approvedRevenue,
        List<MonthlyMetricResponse> monthlyMetrics,
        List<VenueMetricResponse> venueMetrics,
        List<ReservationResponse> recentReservations,
        List<ReservationResponse> upcomingReservationItems
) {
}
