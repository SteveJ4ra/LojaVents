package ec.edu.unl.lojavents.user.application;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.auth.api.dto.UserResponse;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.storage.StoredFile;
import ec.edu.unl.lojavents.user.api.dto.*;
import ec.edu.unl.lojavents.user.domain.*;
import ec.edu.unl.lojavents.user.repository.SolicitudPropietarioRepository;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountApplicationService {

    private final UsuarioRepository usuarioRepository;
    private final SolicitudPropietarioRepository solicitudRepository;
    private final AuditEventRepository auditRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountApplicationService(
            UsuarioRepository usuarioRepository,
            SolicitudPropietarioRepository solicitudRepository,
            AuditEventRepository auditRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse updateProfile(String subject, UpdateProfileRequest request) {
        Usuario usuario = requireUser(subject);
        usuario.actualizarPerfil(request.fullName().trim(), request.phone().trim());
        usuarioRepository.save(usuario);

        audit("PERFIL_ACTUALIZADO", usuario.getId(), "El usuario actualizó su perfil.",
                Map.of("email", usuario.getEmail()));
        return UserResponse.from(usuario);
    }

    @Transactional
    public void changePassword(String subject, ChangePasswordRequest request) {
        Usuario usuario = requireUser(subject);
        if (!passwordEncoder.matches(request.currentPassword(), usuario.getPasswordHash())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "CURRENT_PASSWORD_INCORRECT",
                    "La contraseña actual no es correcta."
            );
        }
        if (passwordEncoder.matches(request.newPassword(), usuario.getPasswordHash())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_NOT_CHANGED",
                    "La nueva contraseña debe ser diferente de la actual."
            );
        }

        usuario.actualizarPasswordHash(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);
        audit("PASSWORD_ACTUALIZADA", usuario.getId(), "El usuario cambió su contraseña.", Map.of());
    }

    @Transactional
    public void deactivateOwnAccount(String subject) {
        Usuario usuario = requireUser(subject);
        if (usuario.getRoles().contains(Rol.ADMINISTRADOR)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "ADMIN_DEACTIVATION_NOT_ALLOWED",
                    "La cuenta administradora de demostración no puede darse de baja desde el perfil."
            );
        }
        usuario.cambiarEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
        audit("CUENTA_DADA_DE_BAJA", usuario.getId(), "El usuario dio de baja su cuenta.", Map.of());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return usuarioRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse changeUserStatus(
            String adminSubject,
            UUID userId,
            UserStatusRequest request
    ) {
        UUID adminId = parseSubject(adminSubject);
        if (adminId.equals(userId)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "SELF_STATUS_CHANGE_NOT_ALLOWED",
                    "No puedes cambiar el estado de tu propia cuenta administradora."
            );
        }

        Usuario usuario = requireUser(userId);
        if (usuario.getRoles().contains(Rol.ADMINISTRADOR)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN_ACCOUNT_PROTECTED",
                    "No se puede modificar el estado de otra cuenta administradora."
            );
        }

        usuario.cambiarEstado(request.status());
        usuarioRepository.save(usuario);

        audit("ADMIN_USUARIO_ESTADO_CAMBIADO", adminId,
                "Un administrador cambió el estado de una cuenta.",
                Map.of("userId", userId.toString(), "status", request.status().name()));
        return UserResponse.from(usuario);
    }

    @Transactional
    public OwnerRequestResponse submitOwnerRequest(
            String subject,
            String identification,
            String notes,
            StoredFile document
    ) {
        Usuario usuario = requireUser(subject);
        if (usuario.getRoles().contains(Rol.PROPIETARIO)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "ALREADY_OWNER",
                    "La cuenta ya tiene el rol de propietario."
            );
        }
        if (solicitudRepository.existsByUsuarioIdAndEstado(
                usuario.getId(), EstadoSolicitudPropietario.PENDIENTE)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "OWNER_REQUEST_ALREADY_PENDING",
                    "Ya existe una solicitud pendiente para esta cuenta."
            );
        }

        SolicitudPropietario solicitud = new SolicitudPropietario(
                usuario,
                identification.trim(),
                document.fileName(),
                document.id(),
                document.contentType(),
                document.size(),
                notes.trim()
        );
        solicitudRepository.save(solicitud);
        usuario.actualizarVerificacionPropietario(EstadoVerificacionPropietario.PENDIENTE);
        usuarioRepository.save(usuario);

        audit("SOLICITUD_PROPIETARIO_ENVIADA", usuario.getId(),
                "El usuario solicitó el rol de propietario.",
                Map.of("requestId", solicitud.getId().toString()));
        return OwnerRequestResponse.from(solicitud);
    }

    @Transactional(readOnly = true)
    public Optional<OwnerRequestResponse> currentOwnerRequest(String subject) {
        UUID userId = parseSubject(subject);
        return solicitudRepository.findTopByUsuarioIdOrderByCreadoEnDesc(userId)
                .map(OwnerRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public List<OwnerRequestResponse> listOwnerRequests(EstadoSolicitudPropietario status) {
        List<SolicitudPropietario> solicitudes = status == null
                ? solicitudRepository.findAllByOrderByCreadoEnDesc()
                : solicitudRepository.findByEstadoOrderByCreadoEnAsc(status);
        return solicitudes.stream().map(OwnerRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public String ownerRequestDocumentId(UUID requestId) {
        SolicitudPropietario solicitud = solicitudRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "OWNER_REQUEST_NOT_FOUND",
                        "La solicitud de propietario no existe."
                ));
        if (solicitud.getDocumentoArchivoId() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "La solicitud no tiene un documento disponible.");
        }
        return solicitud.getDocumentoArchivoId();
    }

    @Transactional
    public OwnerRequestResponse reviewOwnerRequest(
            String adminSubject,
            UUID requestId,
            OwnerRequestReviewRequest request
    ) {
        Usuario admin = requireUser(adminSubject);
        SolicitudPropietario solicitud = solicitudRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "OWNER_REQUEST_NOT_FOUND",
                        "La solicitud de propietario no existe."
                ));

        if (solicitud.getEstado() != EstadoSolicitudPropietario.PENDIENTE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "OWNER_REQUEST_ALREADY_REVIEWED",
                    "La solicitud ya fue revisada."
            );
        }

        Usuario solicitante = solicitud.getUsuario();
        if (request.decision() == DecisionSolicitudPropietario.APROBAR) {
            solicitud.aprobar(admin, request.comment());
            solicitante.agregarRol(Rol.PROPIETARIO);
            solicitante.actualizarVerificacionPropietario(EstadoVerificacionPropietario.APROBADA);
        } else {
            solicitud.rechazar(admin, request.comment());
            solicitante.actualizarVerificacionPropietario(EstadoVerificacionPropietario.RECHAZADA);
        }

        solicitudRepository.save(solicitud);
        usuarioRepository.save(solicitante);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("requestId", requestId.toString());
        details.put("userId", solicitante.getId().toString());
        details.put("decision", request.decision().name());
        audit("ADMIN_SOLICITUD_PROPIETARIO_REVISADA", admin.getId(),
                "Un administrador revisó una solicitud de propietario.", details);

        return OwnerRequestResponse.from(solicitud);
    }

    private Usuario requireUser(String subject) {
        return requireUser(parseSubject(subject));
    }

    private Usuario requireUser(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "USER_NOT_FOUND",
                        "El usuario de la sesión ya no existe."
                ));
    }

    private UUID parseSubject(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido.");
        }
    }

    private void audit(String type, UUID actor, String message, Map<String, Object> data) {
        auditRepository.save(new AuditEvent(type, actor.toString(), message, data));
    }
}
