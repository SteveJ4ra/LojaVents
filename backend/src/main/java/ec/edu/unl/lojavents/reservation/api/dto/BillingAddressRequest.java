package ec.edu.unl.lojavents.reservation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BillingAddressRequest(
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 120) String neighborhood,
        @NotBlank @Size(max = 300) String street
) {
}
