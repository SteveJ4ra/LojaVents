package ec.edu.unl.lojavents.auth.api;

import ec.edu.unl.lojavents.auth.api.dto.AuthResponse;
import ec.edu.unl.lojavents.auth.api.dto.LoginRequest;
import ec.edu.unl.lojavents.auth.api.dto.RegisterRequest;
import ec.edu.unl.lojavents.auth.api.dto.UserResponse;
import ec.edu.unl.lojavents.auth.application.AuthApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@AuthenticationPrincipal Jwt jwt) {
        return authService.refresh(jwt.getSubject());
    }

    @GetMapping("/me")
    public UserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        return authService.currentUser(jwt.getSubject());
    }
}
