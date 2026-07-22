package ec.edu.unl.lojavents.user.repository;

import ec.edu.unl.lojavents.user.domain.EstadoSolicitudPropietario;
import ec.edu.unl.lojavents.user.domain.SolicitudPropietario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudPropietarioRepository extends JpaRepository<SolicitudPropietario, UUID> {

    @EntityGraph(attributePaths = {"usuario", "revisadoPor"})
    Optional<SolicitudPropietario> findTopByUsuarioIdOrderByCreadoEnDesc(UUID usuarioId);

    boolean existsByUsuarioIdAndEstado(UUID usuarioId, EstadoSolicitudPropietario estado);

    long countByEstado(EstadoSolicitudPropietario estado);

    @EntityGraph(attributePaths = {"usuario", "revisadoPor"})
    List<SolicitudPropietario> findByEstadoOrderByCreadoEnAsc(EstadoSolicitudPropietario estado);

    @EntityGraph(attributePaths = {"usuario", "revisadoPor"})
    List<SolicitudPropietario> findAllByOrderByCreadoEnDesc();

    @Override
    @EntityGraph(attributePaths = {"usuario", "revisadoPor"})
    Optional<SolicitudPropietario> findById(UUID id);
}
