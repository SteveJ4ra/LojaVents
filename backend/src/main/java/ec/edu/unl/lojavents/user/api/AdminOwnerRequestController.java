package ec.edu.unl.lojavents.user.api;

import ec.edu.unl.lojavents.user.api.dto.OwnerRequestResponse;
import ec.edu.unl.lojavents.user.api.dto.OwnerRequestReviewRequest;
import ec.edu.unl.lojavents.user.application.UserAccountApplicationService;
import ec.edu.unl.lojavents.user.domain.EstadoSolicitudPropietario;
import ec.edu.unl.lojavents.storage.MediaStorageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/solicitudes-propietario")
public class AdminOwnerRequestController {

    private final UserAccountApplicationService service;
    private final MediaStorageService storage;

    public AdminOwnerRequestController(UserAccountApplicationService service, MediaStorageService storage) {
        this.service = service;
        this.storage = storage;
    }

    @GetMapping
    public List<OwnerRequestResponse> list(
            @RequestParam(required = false) EstadoSolicitudPropietario status
    ) {
        return service.listOwnerRequests(status);
    }

    @PatchMapping("/{id}")
    public OwnerRequestResponse review(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody OwnerRequestReviewRequest request
    ) {
        return service.reviewOwnerRequest(jwt.getSubject(), id, request);
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<Resource> document(@PathVariable UUID id) {
        var resource = storage.read(service.ownerRequestDocumentId(id));
        MediaType type = MediaType.parseMediaType(resource.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE : resource.getContentType());
        return ResponseEntity.ok()
                .contentType(type)
                .header("Content-Disposition", ContentDisposition.inline().filename(resource.getFilename()).build().toString())
                .body(resource);
    }
}
