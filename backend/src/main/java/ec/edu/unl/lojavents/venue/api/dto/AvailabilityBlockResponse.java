package ec.edu.unl.lojavents.venue.api.dto;

import ec.edu.unl.lojavents.venue.domain.BloqueDisponibilidad;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record AvailabilityBlockResponse(
        UUID id,
        String date,
        String startTime,
        String endTime,
        String reason
) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static AvailabilityBlockResponse from(BloqueDisponibilidad bloque) {
        return new AvailabilityBlockResponse(
                bloque.getId(),
                bloque.getFecha().toString(),
                bloque.getHoraInicio().format(TIME_FORMAT),
                bloque.getHoraFin().format(TIME_FORMAT),
                bloque.getMotivo()
        );
    }
}
