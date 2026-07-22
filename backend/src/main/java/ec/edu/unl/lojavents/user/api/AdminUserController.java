package ec.edu.unl.lojavents.user.api;

import ec.edu.unl.lojavents.auth.api.dto.UserResponse;
import ec.edu.unl.lojavents.user.api.dto.UserStatusRequest;
import ec.edu.unl.lojavents.user.application.UserAccountApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class AdminUserController {

    private final UserAccountApplicationService service;

    public AdminUserController(UserAccountApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> list() {
        return service.listUsers();
    }

    @PatchMapping("/{id}/estado")
    public UserResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        return service.changeUserStatus(jwt.getSubject(), id, request);
    }
}
