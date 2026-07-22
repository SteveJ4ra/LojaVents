package ec.edu.unl.lojavents.auth.api.dto;

import ec.edu.unl.lojavents.user.domain.Usuario;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        List<String> roles,
        String status,
        String ownerVerificationStatus,
        OffsetDateTime createdAt
) {
    public static UserResponse from(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();

        return new UserResponse(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getEmail(),
                usuario.getTelefono(),
                roles,
                usuario.getEstado().name(),
                usuario.getEstadoVerificacionPropietario().name(),
                usuario.getCreadoEn()
        );
    }
}
