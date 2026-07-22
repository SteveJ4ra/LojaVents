package ec.edu.unl.lojavents.system.api;

import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sistema")
public class SystemController {

    private final UsuarioRepository usuarioRepository;
    private final AuditEventRepository auditEventRepository;

    public SystemController(
            UsuarioRepository usuarioRepository,
            AuditEventRepository auditEventRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping("/salud")
    public ResponseEntity<Map<String, Object>> salud() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", "OK");
        body.put("servicio", "LojaVents Backend");
        body.put("fecha", OffsetDateTime.now());
        body.put("postgresUsuarios", usuarioRepository.count());
        body.put("mongoEventos", auditEventRepository.count());
        return ResponseEntity.ok(body);
    }
}
