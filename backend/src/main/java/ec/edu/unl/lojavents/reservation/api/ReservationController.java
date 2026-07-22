package ec.edu.unl.lojavents.reservation.api;

import ec.edu.unl.lojavents.reservation.api.dto.CreateReservationRequest;
import ec.edu.unl.lojavents.reservation.api.dto.PaymentSimulationRequest;
import ec.edu.unl.lojavents.reservation.api.dto.ReservationResponse;
import ec.edu.unl.lojavents.reservation.application.ReservationApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservationController {

    private final ReservationApplicationService service;

    public ReservationController(ReservationApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return service.createDraft(jwt.getSubject(), request);
    }

    @PostMapping("/{id}/pago-simulado")
    public ReservationResponse simulatePayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody PaymentSimulationRequest request
    ) {
        return service.simulatePayment(jwt.getSubject(), id, request);
    }

    @GetMapping("/mias")
    public List<ReservationResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.myReservations(jwt.getSubject());
    }
}
