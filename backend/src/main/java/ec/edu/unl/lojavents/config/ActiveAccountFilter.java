package ec.edu.unl.lojavents.config;

import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class ActiveAccountFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public ActiveAccountFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token
                && token.isAuthenticated()) {
            boolean active = false;
            try {
                UUID userId = UUID.fromString(token.getToken().getSubject());
                active = usuarioRepository.findById(userId)
                        .map(user -> user.isActivo())
                        .orElse(false);
            } catch (IllegalArgumentException ignored) {
                active = false;
            }

            if (!active) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType("application/problem+json");
                response.getWriter().write("""
                        {"title":"Cuenta no disponible","status":403,"detail":"La cuenta está suspendida o inactiva.","code":"ACCOUNT_INACTIVE"}
                        """);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
