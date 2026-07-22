package ec.edu.unl.lojavents.dashboard.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardResponse(
        long totalUsers,
        long activeUsers,
        long suspendedUsers,
        long inactiveUsers,
        long clientUsers,
        long ownerUsers,
        long totalVenues,
        long activeVenues,
        long inactiveVenues,
        long totalReservations,
        long completedReservations,
        long rejectedReservations,
        long cancelledReservations,
        BigDecimal approvedRevenue,
        BigDecimal serviceFeeRevenue,
        long totalReviews,
        long pendingOwnerRequests,
        List<MonthlyMetricResponse> monthlyMetrics,
        List<VenueMetricResponse> topVenues,
        List<ActivityItemResponse> recentActivity
) {
}
