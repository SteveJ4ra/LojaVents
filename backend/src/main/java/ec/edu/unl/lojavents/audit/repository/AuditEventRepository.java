package ec.edu.unl.lojavents.audit.repository;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findTop10ByOrderByCreadoEnDesc();
}
