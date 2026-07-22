package ec.edu.unl.lojavents.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(min = 3, max = 120, message = "Los nombres deben tener entre 3 y 120 caracteres.")
        String fullName,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no es válido.")
        @Size(max = 180, message = "El correo es demasiado largo.")
        String email,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Pattern(regexp = "^[0-9]{9,10}$", message = "El teléfono debe contener 9 o 10 dígitos.")
        String phone,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
        String password
) {
}
