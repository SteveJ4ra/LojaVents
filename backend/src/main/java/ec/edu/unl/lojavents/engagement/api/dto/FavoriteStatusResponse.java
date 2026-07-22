package ec.edu.unl.lojavents.engagement.api.dto;

import java.util.UUID;

public record FavoriteStatusResponse(
        UUID venueId,
        boolean favorite
) {
}
