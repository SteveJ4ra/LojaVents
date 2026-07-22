package ec.edu.unl.lojavents.dashboard.api.dto;

import java.math.BigDecimal;

public record MonthlyMetricResponse(
        String key,
        String label,
        long reservations,
        BigDecimal revenue
) {
}
