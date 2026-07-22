package ec.edu.unl.lojavents.engagement.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @Min(value = 1, message = "La calificación mínima es 1.")
        @Max(value = 5, message = "La calificación máxima es 5.")
        int rating,

        @NotBlank(message = "Escribe un comentario.")
        @Size(min = 10, max = 2000, message = "La reseña debe tener entre 10 y 2000 caracteres.")
        String comment
) {
}
