package ec.edu.unl.lojavents.dashboard.api;

import ec.edu.unl.lojavents.dashboard.api.dto.AdminDashboardResponse;
import ec.edu.unl.lojavents.dashboard.application.DashboardApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final DashboardApplicationService service;

    public AdminDashboardController(DashboardApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public AdminDashboardResponse getDashboard() {
        return service.adminDashboard();
    }
}
