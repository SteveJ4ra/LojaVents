package ec.edu.unl.lojavents.reservation.api;

import ec.edu.unl.lojavents.reservation.api.dto.ReservationResponse;
import ec.edu.unl.lojavents.reservation.application.ReservationApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/propietario/reservas")
public class OwnerReservationController {

    private final ReservationApplicationService service;

    public OwnerReservationController(ReservationApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReservationResponse> received(@AuthenticationPrincipal Jwt jwt) {
        return service.ownerReservations(jwt.getSubject());
    }
}
