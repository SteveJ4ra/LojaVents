package ec.edu.unl.lojavents.venue.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record SaveVenueRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 240) String shortDescription,
        @NotBlank @Size(max = 5000) String description,
        @NotBlank @Size(max = 120) String neighborhood,
        @NotBlank @Size(max = 240) String address,
        @NotNull @DecimalMin(value = "1.00") @Digits(integer = 8, fraction = 2) BigDecimal pricePerHour,
        @Min(1) @Max(10000) int capacity,
        @NotEmpty @Size(max = 20) List<@NotBlank @Size(max = 80) String> eventTypes,
        @NotEmpty @Size(max = 30) List<@NotBlank @Size(max = 120) String> amenities,
        @NotEmpty @Size(max = 30) List<@NotBlank @Size(max = 500) String> rules,
        @NotBlank @Size(max = 3000) String cancellationPolicy,
        @NotEmpty @Size(max = 10) List<@NotBlank @Size(max = 500) String> images
) {
}
