package ec.edu.unl.lojavents.engagement.repository;

import ec.edu.unl.lojavents.engagement.domain.Resena;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResenaRepository extends JpaRepository<Resena, UUID> {

    @EntityGraph(attributePaths = {"cliente", "reserva", "local"})
    List<Resena> findByLocal_IdOrderByCreadoEnDesc(UUID localId);

    boolean existsByReserva_Id(UUID reservaId);
}
