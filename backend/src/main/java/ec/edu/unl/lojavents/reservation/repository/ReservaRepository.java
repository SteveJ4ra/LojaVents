package ec.edu.unl.lojavents.reservation.repository;

import ec.edu.unl.lojavents.reservation.domain.EstadoReserva;
import ec.edu.unl.lojavents.reservation.domain.Reserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    @EntityGraph(attributePaths = {"local", "cliente", "pago"})
    @Query("select r from Reserva r order by r.creadoEn desc")
    List<Reserva> findAllDetailed();

    @EntityGraph(attributePaths = {"local", "cliente", "pago"})
    @Query("select r from Reserva r where r.cliente.id = :clienteId order by r.creadoEn desc")
    List<Reserva> findMine(@Param("clienteId") UUID clienteId);

    @EntityGraph(attributePaths = {"local", "cliente", "pago"})
    @Query("select r from Reserva r where r.local.propietario.id = :ownerId order by r.creadoEn desc")
    List<Reserva> findForOwner(@Param("ownerId") UUID ownerId);

    @EntityGraph(attributePaths = {"local", "cliente", "pago"})
    @Query("select r from Reserva r where r.id = :id and r.cliente.id = :clienteId")
    Optional<Reserva> findOwnedByClient(
            @Param("id") UUID id,
            @Param("clienteId") UUID clienteId
    );

    @Query("""
            select r from Reserva r
            where r.local.id = :localId
              and r.periodo.fecha = :date
              and r.estado = :status
            """)
    List<Reserva> findByVenueDateAndStatus(
            @Param("localId") UUID localId,
            @Param("date") LocalDate date,
            @Param("status") EstadoReserva status
    );

    @Query("""
            select r from Reserva r
            where r.local.id = :localId
              and r.periodo.fecha in :dates
              and r.estado = :status
            """)
    List<Reserva> findByVenueDatesAndStatus(
            @Param("localId") UUID localId,
            @Param("dates") List<LocalDate> dates,
            @Param("status") EstadoReserva status
    );

    boolean existsByCliente_IdAndEstadoAndPeriodo_FechaBefore(
            UUID clienteId,
            EstadoReserva estado,
            LocalDate fecha
    );
}
