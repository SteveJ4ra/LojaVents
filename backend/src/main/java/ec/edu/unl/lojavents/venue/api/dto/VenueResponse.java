package ec.edu.unl.lojavents.venue.api.dto;

import ec.edu.unl.lojavents.venue.domain.LocalEvento;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record VenueResponse(
        UUID id,
        UUID ownerId,
        String name,
        String shortDescription,
        String description,
        String neighborhood,
        String address,
        BigDecimal pricePerHour,
        int capacity,
        BigDecimal rating,
        int reviewCount,
        List<String> eventTypes,
        List<String> amenities,
        List<String> rules,
        String cancellationPolicy,
        List<String> images,
        boolean featured,
        boolean active,
        boolean pendingReview,
        List<AvailabilityBlockResponse> blockedSlots
) {
    public static VenueResponse from(LocalEvento local) {
        return new VenueResponse(
                local.getId(),
                local.getPropietario().getId(),
                local.getNombre(),
                local.getDescripcionCorta(),
                local.getDescripcion(),
                local.getSector(),
                local.getDireccion(),
                local.getPrecioHora(),
                local.getCapacidad(),
                local.getCalificacion(),
                local.getTotalResenas(),
                List.copyOf(local.getTiposEvento()),
                List.copyOf(local.getAmenidades()),
                List.copyOf(local.getReglas()),
                local.getPoliticaCancelacion(),
                List.copyOf(local.getImagenes()),
                local.isDestacado(),
                local.isActivo(),
                local.isPendienteRevision(),
                local.getBloqueos().stream().map(AvailabilityBlockResponse::from).toList()
        );
    }
}
