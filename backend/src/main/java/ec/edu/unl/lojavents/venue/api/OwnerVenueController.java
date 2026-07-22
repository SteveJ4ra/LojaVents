package ec.edu.unl.lojavents.venue.api;

import ec.edu.unl.lojavents.venue.api.dto.*;
import ec.edu.unl.lojavents.venue.application.VenueApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/propietario/locales")
public class OwnerVenueController {

    private final VenueApplicationService service;

    public OwnerVenueController(VenueApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<VenueResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.ownerVenues(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public VenueResponse detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.ownerDetail(jwt.getSubject(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SaveVenueRequest request
    ) {
        return service.create(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public VenueResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody SaveVenueRequest request
    ) {
        return service.update(jwt.getSubject(), id, request);
    }

    @PatchMapping("/{id}/estado")
    public VenueResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody VenueStatusRequest request
    ) {
        return service.changeOwnerStatus(jwt.getSubject(), id, request);
    }

    @PostMapping("/{id}/bloqueos")
    public VenueResponse addBlock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody AvailabilityBlockRequest request
    ) {
        return service.addBlock(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/{id}/bloqueos/{blockId}")
    public VenueResponse removeBlock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @PathVariable UUID blockId
    ) {
        return service.removeBlock(jwt.getSubject(), id, blockId);
    }
}
