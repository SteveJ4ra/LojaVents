package ec.edu.unl.lojavents.user.api.dto;

import ec.edu.unl.lojavents.user.domain.TipoDocumentoIdentidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OwnerRequestCreateRequest(
        @NotNull(message = "El tipo de documento es obligatorio.")
        TipoDocumentoIdentidad documentType,

        @NotBlank(message = "La identificación es obligatoria.")
        @Size(min = 5, max = 20, message = "La identificación debe tener entre 5 y 20 caracteres.")
        String identification,

        @NotBlank(message = "Debes indicar el documento de respaldo.")
        @Size(max = 255, message = "La referencia del documento es demasiado larga.")
        String documentReference,

        @NotBlank(message = "La descripción del local es obligatoria.")
        @Size(min = 15, max = 1200, message = "La descripción debe tener entre 15 y 1200 caracteres.")
        String notes
) {
}
