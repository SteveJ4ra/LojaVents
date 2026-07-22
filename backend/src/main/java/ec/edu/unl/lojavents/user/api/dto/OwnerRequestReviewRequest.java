package ec.edu.unl.lojavents.user.api.dto;

import ec.edu.unl.lojavents.user.domain.DecisionSolicitudPropietario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OwnerRequestReviewRequest(
        @NotNull(message = "La decisión es obligatoria.")
        DecisionSolicitudPropietario decision,

        @Size(max = 600, message = "El comentario no puede superar 600 caracteres.")
        String comment
) {
}
