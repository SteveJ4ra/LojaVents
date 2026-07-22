package ec.edu.unl.lojavents.venue.api;

import ec.edu.unl.lojavents.venue.api.dto.VenueResponse;
import ec.edu.unl.lojavents.venue.api.dto.VenueStatusRequest;
import ec.edu.unl.lojavents.venue.application.VenueApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/locales")
public class AdminVenueController {

    private final VenueApplicationService service;

    public AdminVenueController(VenueApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<VenueResponse> list() {
        return service.adminVenues();
    }

    @PatchMapping("/{id}/estado")
    public VenueResponse changeStatus(
            @PathVariable UUID id,
            @RequestBody VenueStatusRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.changeAdminStatus(id, request, jwt.getSubject());
    }
}
