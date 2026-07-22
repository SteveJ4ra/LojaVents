package ec.edu.unl.lojavents.auth.application;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.auth.api.dto.AuthResponse;
import ec.edu.unl.lojavents.auth.api.dto.LoginRequest;
import ec.edu.unl.lojavents.auth.api.dto.RegisterRequest;
import ec.edu.unl.lojavents.auth.api.dto.UserResponse;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.user.domain.EstadoUsuario;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthApplicationService {

    private final UsuarioRepository usuarioRepository;
    private final AuditEventRepository auditEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthApplicationService(
            UsuarioRepository usuarioRepository,
            AuditEventRepository auditEventRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.auditEventRepository = auditEventRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "EMAIL_ALREADY_EXISTS",
                    "Ya existe una cuenta con ese correo."
            );
        }

        Usuario usuario = new Usuario(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.phone().trim()
        );
        usuarioRepository.save(usuario);

        auditEventRepository.save(new AuditEvent(
                "USUARIO_REGISTRADO",
                usuario.getId().toString(),
                "Se registró una nueva cuenta de cliente.",
                Map.of("email", usuario.getEmail())
        ));

        return createResponse(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(this::invalidCredentials);

        validateActive(usuario);

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw invalidCredentials();
        }

        auditEventRepository.save(new AuditEvent(
                "INICIO_SESION",
                usuario.getId().toString(),
                "Inicio de sesión correcto.",
                Map.of("email", usuario.getEmail())
        ));

        return createResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String subject) {
        Usuario usuario = findSessionUser(subject);
        validateActive(usuario);
        return UserResponse.from(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String subject) {
        Usuario usuario = findSessionUser(subject);
        validateActive(usuario);
        return createResponse(usuario);
    }

    private Usuario findSessionUser(String subject) {
        UUID id;
        try {
            id = UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido.");
        }

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "USER_NOT_FOUND",
                        "El usuario de la sesión ya no existe."
                ));
    }

    private void validateActive(Usuario usuario) {
        if (usuario.getEstado() == EstadoUsuario.SUSPENDIDO) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "ACCOUNT_SUSPENDED",
                    "La cuenta fue suspendida por un administrador."
            );
        }
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "ACCOUNT_INACTIVE",
                    "La cuenta no se encuentra activa."
            );
        }
    }

    private AuthResponse createResponse(Usuario usuario) {
        JwtService.TokenResult token = jwtService.createToken(usuario);
        return new AuthResponse(
                token.value(),
                "Bearer",
                token.expiresInSeconds(),
                UserResponse.from(usuario)
        );
    }

    private ApiException invalidCredentials() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Correo o contraseña incorrectos."
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
