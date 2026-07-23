package ec.edu.unl.lojavents.venue.repository;

import ec.edu.unl.lojavents.venue.domain.EstadoPublicacionLocal;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocalEventoRepository extends JpaRepository<LocalEvento, UUID> {
    List<LocalEvento> findByEstadoPublicacionOrderByDestacadoDescNombreAsc(
            EstadoPublicacionLocal estadoPublicacion
    );
    List<LocalEvento> findByPropietarioIdOrderByCreadoEnDesc(UUID propietarioId);
    Optional<LocalEvento> findByIdAndPropietarioId(UUID id, UUID propietarioId);
    List<LocalEvento> findAllByOrderByCreadoEnDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LocalEvento l where l.id = :id")
    Optional<LocalEvento> findByIdForUpdate(@Param("id") UUID id);
}
