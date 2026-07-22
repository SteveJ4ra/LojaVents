package ec.edu.unl.lojavents.engagement.repository;

import ec.edu.unl.lojavents.engagement.domain.Favorito;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoritoRepository extends JpaRepository<Favorito, UUID> {

    @EntityGraph(attributePaths = {"local", "local.propietario", "local.bloqueos"})
    List<Favorito> findByCliente_IdOrderByCreadoEnDesc(UUID clienteId);

    Optional<Favorito> findByCliente_IdAndLocal_Id(UUID clienteId, UUID localId);

    boolean existsByCliente_IdAndLocal_Id(UUID clienteId, UUID localId);
}
