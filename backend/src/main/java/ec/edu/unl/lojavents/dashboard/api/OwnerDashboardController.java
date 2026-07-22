package ec.edu.unl.lojavents.dashboard.api;

import ec.edu.unl.lojavents.dashboard.api.dto.OwnerDashboardResponse;
import ec.edu.unl.lojavents.dashboard.application.DashboardApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/propietario/dashboard")
public class OwnerDashboardController {

    private final DashboardApplicationService service;

    public OwnerDashboardController(DashboardApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public OwnerDashboardResponse getDashboard(@AuthenticationPrincipal Jwt jwt) {
        return service.ownerDashboard(jwt.getSubject());
    }
}
