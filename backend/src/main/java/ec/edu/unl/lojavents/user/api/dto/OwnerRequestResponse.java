package ec.edu.unl.lojavents.user.api.dto;

import ec.edu.unl.lojavents.user.domain.SolicitudPropietario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OwnerRequestResponse(
        UUID id,
        UUID userId,
        String userFullName,
        String userEmail,
        String documentType,
        String identification,
        String documentReference,
        boolean hasDocument,
        String notes,
        String status,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        String reviewerName,
        String adminComment
) {
    public static OwnerRequestResponse from(SolicitudPropietario solicitud) {
        return new OwnerRequestResponse(
                solicitud.getId(),
                solicitud.getUsuario().getId(),
                solicitud.getUsuario().getNombres(),
                solicitud.getUsuario().getEmail(),
                solicitud.getTipoDocumento() == null ? null : solicitud.getTipoDocumento().name(),
                solicitud.getIdentificacion(),
                solicitud.getDocumentoNombre() == null ? solicitud.getDocumentoReferencia() : solicitud.getDocumentoNombre(),
                solicitud.getDocumentoArchivoId() != null,
                solicitud.getNotas(),
                solicitud.getEstado().name(),
                solicitud.getCreadoEn(),
                solicitud.getRevisadoEn(),
                solicitud.getRevisadoPor() == null ? null : solicitud.getRevisadoPor().getNombres(),
                solicitud.getComentarioAdmin()
        );
    }
}
