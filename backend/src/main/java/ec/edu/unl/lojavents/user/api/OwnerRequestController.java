package ec.edu.unl.lojavents.user.api;

import ec.edu.unl.lojavents.user.api.dto.OwnerRequestResponse;
import ec.edu.unl.lojavents.user.application.UserAccountApplicationService;
import ec.edu.unl.lojavents.storage.MediaStorageService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/solicitud-propietario")
public class OwnerRequestController {

    private final UserAccountApplicationService service;
    private final MediaStorageService storage;

    public OwnerRequestController(UserAccountApplicationService service, MediaStorageService storage) {
        this.service = service;
        this.storage = storage;
    }

    @GetMapping("/me")
    public ResponseEntity<OwnerRequestResponse> current(@AuthenticationPrincipal Jwt jwt) {
        return service.currentOwnerRequest(jwt.getSubject())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerRequestResponse submit(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @NotBlank @Size(min = 10, max = 30) String identification,
            @RequestParam @NotBlank @Size(min = 15, max = 1200) String notes,
            @RequestParam("document") MultipartFile document
    ) {
        return service.submitOwnerRequest(jwt.getSubject(), identification, notes, storage.storeOwnerDocument(document));
    }
}
