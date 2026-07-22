package ec.edu.unl.lojavents.reservation.api.dto;

public record AvailabilityResponse(
        boolean available,
        String message
) {
    public static AvailabilityResponse availableResult() {
        return new AvailabilityResponse(true, "El horario se encuentra disponible.");
    }

    public static AvailabilityResponse unavailable(String message) {
        return new AvailabilityResponse(false, message);
    }
}
