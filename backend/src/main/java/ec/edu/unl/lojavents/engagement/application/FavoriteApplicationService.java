package ec.edu.unl.lojavents.engagement.application;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.common.api.ApiException;
import ec.edu.unl.lojavents.engagement.api.dto.FavoriteStatusResponse;
import ec.edu.unl.lojavents.engagement.domain.Favorito;
import ec.edu.unl.lojavents.engagement.repository.FavoritoRepository;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.api.dto.VenueResponse;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FavoriteApplicationService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalEventoRepository localRepository;
    private final AuditEventRepository auditRepository;

    public FavoriteApplicationService(
            FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            LocalEventoRepository localRepository,
            AuditEventRepository auditRepository
    ) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.localRepository = localRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    public List<UUID> ids(String subject) {
        UUID userId = parseSubject(subject);
        return favoritoRepository.findByCliente_IdOrderByCreadoEnDesc(userId).stream()
                .filter(item -> item.getLocal().isActivo())
                .map(item -> item.getLocal().getId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> venues(String subject) {
        UUID userId = parseSubject(subject);
        return favoritoRepository.findByCliente_IdOrderByCreadoEnDesc(userId).stream()
                .map(Favorito::getLocal)
                .filter(LocalEvento::isActivo)
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional
    public FavoriteStatusResponse add(String subject, UUID venueId) {
        UUID userId = parseSubject(subject);
        if (favoritoRepository.existsByCliente_IdAndLocal_Id(userId, venueId)) {
            return new FavoriteStatusResponse(venueId, true);
        }

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "USER_NOT_FOUND",
                        "El usuario de la sesión ya no existe."
                ));
        LocalEvento venue = localRepository.findById(venueId)
                .filter(LocalEvento::isActivo)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "VENUE_NOT_FOUND",
                        "El local no existe o no está disponible."
                ));

        favoritoRepository.save(new Favorito(user, venue));
        auditRepository.save(new AuditEvent(
                "FAVORITO_AGREGADO",
                userId.toString(),
                "El usuario agregó un local a favoritos.",
                Map.of("venueId", venueId.toString())
        ));
        return new FavoriteStatusResponse(venueId, true);
    }

    @Transactional
    public FavoriteStatusResponse remove(String subject, UUID venueId) {
        UUID userId = parseSubject(subject);
        favoritoRepository.findByCliente_IdAndLocal_Id(userId, venueId)
                .ifPresent(favoritoRepository::delete);

        auditRepository.save(new AuditEvent(
                "FAVORITO_ELIMINADO",
                userId.toString(),
                "El usuario eliminó un local de favoritos.",
                Map.of("venueId", venueId.toString())
        ));
        return new FavoriteStatusResponse(venueId, false);
    }

    private UUID parseSubject(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido.");
        }
    }
}
