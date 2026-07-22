package ec.edu.unl.lojavents.user.api.dto;

import ec.edu.unl.lojavents.user.domain.EstadoUsuario;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull(message = "El estado es obligatorio.")
        EstadoUsuario status
) {
}
