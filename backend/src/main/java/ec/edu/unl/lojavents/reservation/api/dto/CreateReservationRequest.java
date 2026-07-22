package ec.edu.unl.lojavents.reservation.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID venueId,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @Min(1) @Max(12) int durationHours,
        @Min(1) @Max(10000) int attendees,
        @NotNull @Valid BillingAddressRequest billingAddress,
        @AssertTrue(message = "Debes aceptar las reglas del local.") boolean acceptedRules,
        @AssertTrue(message = "Debes aceptar la política de cancelación.") boolean acceptedCancellation
) {
}
