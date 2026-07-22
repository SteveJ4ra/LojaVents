package ec.edu.unl.lojavents.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(min = 3, max = 120, message = "Los nombres deben tener entre 3 y 120 caracteres.")
        String fullName,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(min = 7, max = 30, message = "El teléfono debe tener entre 7 y 30 caracteres.")
        String phone
) {
}
