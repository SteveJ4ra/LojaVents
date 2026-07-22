package ec.edu.unl.lojavents.venue.api;

import ec.edu.unl.lojavents.reservation.api.dto.AvailabilityResponse;
import ec.edu.unl.lojavents.reservation.application.ReservationAvailabilityService;
import ec.edu.unl.lojavents.venue.api.dto.VenueResponse;
import ec.edu.unl.lojavents.venue.application.VenueApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locales")
public class PublicVenueController {

    private final VenueApplicationService service;
    private final ReservationAvailabilityService availabilityService;

    public PublicVenueController(
            VenueApplicationService service,
            ReservationAvailabilityService availabilityService
    ) {
        this.service = service;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<VenueResponse> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Integer attendees,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.search(text, eventType, attendees, maxPrice, date);
    }

    @GetMapping("/tipos-evento")
    public List<String> eventTypes() {
        return service.eventTypes();
    }

    @GetMapping("/{id}")
    public VenueResponse detail(@PathVariable UUID id) {
        return service.publicDetail(id);
    }

    @GetMapping("/{id}/disponibilidad")
    public AvailabilityResponse availability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam int durationHours
    ) {
        return availabilityService.check(id, date, startTime, durationHours);
    }
}
