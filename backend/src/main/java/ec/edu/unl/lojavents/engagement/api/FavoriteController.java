package ec.edu.unl.lojavents.engagement.api;

import ec.edu.unl.lojavents.engagement.api.dto.FavoriteStatusResponse;
import ec.edu.unl.lojavents.engagement.application.FavoriteApplicationService;
import ec.edu.unl.lojavents.venue.api.dto.VenueResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favoritos")
public class FavoriteController {

    private final FavoriteApplicationService service;

    public FavoriteController(FavoriteApplicationService service) {
        this.service = service;
    }

    @GetMapping("/ids")
    List<UUID> ids(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return service.ids(jwt.getSubject());
    }

    @GetMapping
    List<VenueResponse> venues(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return service.venues(jwt.getSubject());
    }

    @PostMapping("/{venueId}")
    FavoriteStatusResponse add(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID venueId
    ) {
        return service.add(jwt.getSubject(), venueId);
    }

    @DeleteMapping("/{venueId}")
    FavoriteStatusResponse remove(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID venueId
    ) {
        return service.remove(jwt.getSubject(), venueId);
    }
}
