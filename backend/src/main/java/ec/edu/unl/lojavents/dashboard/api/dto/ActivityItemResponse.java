package ec.edu.unl.lojavents.dashboard.api.dto;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;

import java.time.Instant;

public record ActivityItemResponse(
        String id,
        String type,
        String actor,
        String message,
        Instant createdAt
) {
    public static ActivityItemResponse from(AuditEvent event) {
        return new ActivityItemResponse(
                event.getId(),
                event.getTipo(),
                event.getActor(),
                event.getMensaje(),
                event.getCreadoEn()
        );
    }
}
