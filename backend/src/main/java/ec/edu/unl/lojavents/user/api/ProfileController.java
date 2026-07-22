package ec.edu.unl.lojavents.user.api;

import ec.edu.unl.lojavents.auth.api.dto.UserResponse;
import ec.edu.unl.lojavents.user.api.dto.ChangePasswordRequest;
import ec.edu.unl.lojavents.user.api.dto.UpdateProfileRequest;
import ec.edu.unl.lojavents.user.application.UserAccountApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/perfil")
public class ProfileController {

    private final UserAccountApplicationService service;

    public ProfileController(UserAccountApplicationService service) {
        this.service = service;
    }

    @PutMapping
    public UserResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return service.updateProfile(jwt.getSubject(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        service.changePassword(jwt.getSubject(), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@AuthenticationPrincipal Jwt jwt) {
        service.deactivateOwnAccount(jwt.getSubject());
    }
}
