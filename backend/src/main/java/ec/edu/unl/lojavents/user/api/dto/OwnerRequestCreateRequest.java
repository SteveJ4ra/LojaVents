package ec.edu.unl.lojavents.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerRequestCreateRequest(
        @NotBlank(message = "La identificación es obligatoria.")
        @Size(min = 10, max = 30, message = "La identificación debe tener entre 10 y 30 caracteres.")
        String identification,

        @NotBlank(message = "Debes indicar el documento de respaldo.")
        @Size(max = 255, message = "La referencia del documento es demasiado larga.")
        String documentReference,

        @NotBlank(message = "La descripción del local es obligatoria.")
        @Size(min = 15, max = 1200, message = "La descripción debe tener entre 15 y 1200 caracteres.")
        String notes
) {
}
