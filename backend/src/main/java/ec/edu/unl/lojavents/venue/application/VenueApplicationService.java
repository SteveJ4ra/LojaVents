package ec.edu.unl.lojavents.venue.application;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.api.dto.*;
import ec.edu.unl.lojavents.venue.domain.BloqueDisponibilidad;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class VenueApplicationService {

    private final LocalEventoRepository localRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditEventRepository auditRepository;

    public VenueApplicationService(
            LocalEventoRepository localRepository,
            UsuarioRepository usuarioRepository,
            AuditEventRepository auditRepository
    ) {
        this.localRepository = localRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> search(
            String text,
            String eventType,
            Integer attendees,
            BigDecimal maxPrice,
            LocalDate date
    ) {
        String normalizedText = normalize(text);
        String normalizedType = normalize(eventType);

        return localRepository.findByActivoTrueOrderByDestacadoDescNombreAsc().stream()
                .filter(local -> normalizedText == null || matchesText(local, normalizedText))
                .filter(local -> normalizedType == null || local.getTiposEvento().stream()
                        .map(this::normalize)
                        .anyMatch(normalizedType::equals))
                .filter(local -> attendees == null || local.getCapacidad() >= attendees)
                .filter(local -> maxPrice == null || local.getPrecioHora().compareTo(maxPrice) <= 0)
                .filter(local -> date == null || local.getBloqueos().stream()
                        .noneMatch(block -> block.getFecha().equals(date)))
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse publicDetail(UUID id) {
        LocalEvento local = findDetailed(id);
        if (!local.isActivo()) {
            throw notFound();
        }
        return VenueResponse.from(local);
    }

    @Transactional(readOnly = true)
    public List<String> eventTypes() {
        return localRepository.findByActivoTrueOrderByDestacadoDescNombreAsc().stream()
                .flatMap(local -> local.getTiposEvento().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> ownerVenues(String subject) {
        UUID ownerId = parseSubject(subject);
        return localRepository.findByPropietarioIdOrderByCreadoEnDesc(ownerId).stream()
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse ownerDetail(String subject, UUID id) {
        return VenueResponse.from(requireOwnerVenue(subject, id));
    }

    @Transactional
    public VenueResponse create(String subject, SaveVenueRequest request) {
        UUID ownerId = parseSubject(subject);
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "USER_NOT_FOUND",
                        "El usuario de la sesión ya no existe."
                ));

        LocalEvento local = new LocalEvento(owner);
        apply(local, request);
        local.solicitarRevision();
        localRepository.save(local);

        audit("LOCAL_CREADO", ownerId, local, Map.of("name", local.getNombre()));
        return VenueResponse.from(local);
    }

    @Transactional
    public VenueResponse update(String subject, UUID id, SaveVenueRequest request) {
        UUID ownerId = parseSubject(subject);
        LocalEvento local = requireOwnerVenue(ownerId, id);
        apply(local, request);
        local.solicitarRevision();
        localRepository.save(local);

        audit("LOCAL_ACTUALIZADO", ownerId, local, Map.of("name", local.getNombre()));
        return VenueResponse.from(local);
    }

    @Transactional
    public VenueResponse changeOwnerStatus(String subject, UUID id, VenueStatusRequest request) {
        UUID ownerId = parseSubject(subject);
        LocalEvento local = requireOwnerVenue(ownerId, id);
        if (request.active()) {
            local.solicitarRevision();
        } else {
            local.cambiarEstado(false);
        }
        localRepository.save(local);

        audit("LOCAL_ESTADO_CAMBIADO", ownerId, local, Map.of("active", request.active()));
        return VenueResponse.from(local);
    }

    @Transactional
    public VenueResponse addBlock(String subject, UUID id, AvailabilityBlockRequest request) {
        UUID ownerId = parseSubject(subject);
        LocalEvento local = requireOwnerVenue(ownerId, id);
        validateBlock(local, request);

        local.agregarBloqueo(new BloqueDisponibilidad(
                request.date(),
                request.startTime(),
                request.endTime(),
                request.reason().trim()
        ));
        localRepository.save(local);

        audit("LOCAL_BLOQUEO_AGREGADO", ownerId, local, Map.of("date", request.date().toString()));
        return VenueResponse.from(local);
    }

    @Transactional
    public VenueResponse removeBlock(String subject, UUID id, UUID blockId) {
        UUID ownerId = parseSubject(subject);
        LocalEvento local = requireOwnerVenue(ownerId, id);
        if (!local.eliminarBloqueo(blockId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BLOCK_NOT_FOUND", "El bloqueo no existe.");
        }
        localRepository.save(local);

        audit("LOCAL_BLOQUEO_ELIMINADO", ownerId, local, Map.of("blockId", blockId.toString()));
        return VenueResponse.from(local);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> adminVenues() {
        return localRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional
    public VenueResponse changeAdminStatus(UUID id, VenueStatusRequest request, String subject) {
        UUID adminId = parseSubject(subject);
        LocalEvento local = findDetailed(id);
        if (request.active()) {
            local.aprobarRevision();
        } else {
            local.cambiarEstado(false);
        }
        localRepository.save(local);

        audit("ADMIN_LOCAL_ESTADO_CAMBIADO", adminId, local, Map.of("active", request.active()));
        return VenueResponse.from(local);
    }

    private void apply(LocalEvento local, SaveVenueRequest request) {
        local.actualizar(
                request.name().trim(),
                request.shortDescription().trim(),
                request.description().trim(),
                request.neighborhood().trim(),
                request.address().trim(),
                request.pricePerHour(),
                request.capacity(),
                VenueCatalog.eventTypes(request.eventTypes()),
                VenueCatalog.amenities(request.amenities()),
                sanitizeList(request.rules()),
                request.cancellationPolicy().trim(),
                sanitizeList(request.images())
        );
    }

    private List<String> sanitizeList(List<String> values) {
        return values.stream().map(String::trim).filter(value -> !value.isBlank()).toList();
    }

    private void validateBlock(LocalEvento local, AvailabilityBlockRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_TIME_RANGE",
                    "La hora final debe ser posterior a la hora inicial."
            );
        }

        boolean overlap = local.getBloqueos().stream().anyMatch(block ->
                block.getFecha().equals(request.date())
                        && request.startTime().isBefore(block.getHoraFin())
                        && request.endTime().isAfter(block.getHoraInicio())
        );
        if (overlap) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "BLOCK_OVERLAP",
                    "Ese rango se superpone con otro bloqueo del local."
            );
        }
    }

    private boolean matchesText(LocalEvento local, String text) {
        return List.of(
                        local.getNombre(),
                        local.getDescripcionCorta(),
                        local.getSector(),
                        local.getDireccion()
                ).stream().map(this::normalize).anyMatch(value -> value != null && value.contains(text))
                || local.getTiposEvento().stream().map(this::normalize)
                .anyMatch(value -> value != null && value.contains(text));
    }

    private LocalEvento requireOwnerVenue(String subject, UUID id) {
        return requireOwnerVenue(parseSubject(subject), id);
    }

    private LocalEvento requireOwnerVenue(UUID ownerId, UUID id) {
        return localRepository.findByIdAndPropietarioId(id, ownerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "VENUE_NOT_FOUND",
                        "El local no existe o no pertenece al propietario autenticado."
                ));
    }

    private LocalEvento findDetailed(UUID id) {
        return localRepository.findById(id).orElseThrow(this::notFound);
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND", "El local no existe.");
    }

    private UUID parseSubject(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void audit(String type, UUID actor, LocalEvento local, Map<String, Object> data) {
        Map<String, Object> details = new LinkedHashMap<>(data);
        details.put("venueId", local.getId().toString());
        auditRepository.save(new AuditEvent(
                type,
                actor.toString(),
                "Operación realizada sobre un local.",
                details
        ));
    }
}
