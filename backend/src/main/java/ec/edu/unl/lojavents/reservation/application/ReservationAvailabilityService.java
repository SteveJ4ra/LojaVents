package ec.edu.unl.lojavents.reservation.application;

import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.reservation.api.dto.AvailabilityResponse;
import ec.edu.unl.lojavents.reservation.domain.EstadoReserva;
import ec.edu.unl.lojavents.reservation.domain.Reserva;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.venue.domain.BloqueDisponibilidad;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationAvailabilityService {

    private static final ZoneId LOJA_ZONE = ZoneId.of("America/Guayaquil");

    private final LocalEventoRepository localRepository;
    private final ReservaRepository reservaRepository;

    public ReservationAvailabilityService(
            LocalEventoRepository localRepository,
            ReservaRepository reservaRepository
    ) {
        this.localRepository = localRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse check(
            UUID venueId,
            LocalDate date,
            LocalTime startTime,
            int durationHours
    ) {
        LocalEvento local = localRepository.findById(venueId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "VENUE_NOT_FOUND",
                        "El local no existe."
                ));
        return evaluate(local, date, startTime, durationHours);
    }

    public void assertAvailable(
            LocalEvento local,
            LocalDate date,
            LocalTime startTime,
            int durationHours
    ) {
        AvailabilityResponse result = evaluate(local, date, startTime, durationHours);
        if (!result.available()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "SLOT_NOT_AVAILABLE",
                    result.message()
            );
        }
    }

    private AvailabilityResponse evaluate(
            LocalEvento local,
            LocalDate date,
            LocalTime startTime,
            int durationHours
    ) {
        if (!local.isActivo()) {
            return AvailabilityResponse.unavailable("El local no está disponible para reservas.");
        }
        if (durationHours < 1 || durationHours > 12) {
            return AvailabilityResponse.unavailable("La duración debe estar entre 1 y 12 horas.");
        }

        LocalDate today = LocalDate.now(LOJA_ZONE);
        if (date.isBefore(today)) {
            return AvailabilityResponse.unavailable("No se puede reservar una fecha pasada.");
        }
        if (date.equals(today) && !startTime.isAfter(LocalTime.now(LOJA_ZONE))) {
            return AvailabilityResponse.unavailable("La hora de inicio debe ser posterior a la hora actual.");
        }

        int requestedStart = toMinutes(startTime);
        int requestedEnd = requestedStart + durationHours * 60;
        if (requestedEnd > 36 * 60) {
            return AvailabilityResponse.unavailable("La reserva no puede terminar después de medianoche.");
        }

        boolean blocked = local.getBloqueos().stream()
                .anyMatch(block -> overlaps(
                        requestedStart,
                        requestedEnd,
                        relativeMinutes(date, block.getFecha(), block.getHoraInicio()),
                        relativeMinutes(date, block.getFecha(), block.getHoraFin())
                ));
        if (blocked) {
            return AvailabilityResponse.unavailable("El propietario bloqueó ese horario.");
        }

        List<Reserva> confirmed = reservaRepository.findByVenueDatesAndStatus(
                local.getId(),
                List.of(date.minusDays(1), date, date.plusDays(1)),
                EstadoReserva.COMPLETADA
        );
        boolean reserved = confirmed.stream().anyMatch(existing -> overlaps(
                requestedStart,
                requestedEnd,
                relativeMinutes(date, existing.getFecha(), existing.getHoraInicio()),
                relativeMinutes(date, existing.getFecha(), existing.getHoraInicio())
                        + existing.getDuracionHoras() * 60
        ));
        if (reserved) {
            return AvailabilityResponse.unavailable("Ese horario ya fue reservado por otro cliente.");
        }

        return AvailabilityResponse.availableResult();
    }

    private boolean overlaps(int start, int end, int existingStart, int existingEnd) {
        return start < existingEnd && end > existingStart;
    }

    private int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private int relativeMinutes(LocalDate requestedDate, LocalDate slotDate, LocalTime time) {
        return (int) ChronoUnit.DAYS.between(requestedDate, slotDate) * 24 * 60 + toMinutes(time);
    }
}
