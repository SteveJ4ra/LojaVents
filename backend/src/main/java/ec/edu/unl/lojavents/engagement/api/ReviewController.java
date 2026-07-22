package ec.edu.unl.lojavents.engagement.api;

import ec.edu.unl.lojavents.engagement.api.dto.ReviewRequest;
import ec.edu.unl.lojavents.engagement.api.dto.ReviewResponse;
import ec.edu.unl.lojavents.engagement.application.ReviewApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ReviewController {

    private final ReviewApplicationService service;

    public ReviewController(ReviewApplicationService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/locales/{venueId}/resenas")
    List<ReviewResponse> byVenue(@PathVariable UUID venueId) {
        return service.byVenue(venueId);
    }

    @PostMapping("/api/v1/reservas/{reservationId}/resena")
    ReviewResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reservationId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return service.create(jwt.getSubject(), reservationId, request);
    }
}
