package ec.edu.unl.lojavents.dashboard.application;

import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.dashboard.api.dto.*;
import ec.edu.unl.lojavents.engagement.repository.ResenaRepository;
import ec.edu.unl.lojavents.reservation.api.dto.ReservationResponse;
import ec.edu.unl.lojavents.reservation.domain.EstadoReserva;
import ec.edu.unl.lojavents.reservation.domain.Reserva;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.user.domain.EstadoSolicitudPropietario;
import ec.edu.unl.lojavents.user.domain.EstadoUsuario;
import ec.edu.unl.lojavents.user.domain.Rol;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.SolicitudPropietarioRepository;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardApplicationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final UsuarioRepository usuarioRepository;
    private final LocalEventoRepository localRepository;
    private final ReservaRepository reservaRepository;
    private final ResenaRepository resenaRepository;
    private final SolicitudPropietarioRepository solicitudRepository;
    private final AuditEventRepository auditRepository;

    public DashboardApplicationService(
            UsuarioRepository usuarioRepository,
            LocalEventoRepository localRepository,
            ReservaRepository reservaRepository,
            ResenaRepository resenaRepository,
            SolicitudPropietarioRepository solicitudRepository,
            AuditEventRepository auditRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.localRepository = localRepository;
        this.reservaRepository = reservaRepository;
        this.resenaRepository = resenaRepository;
        this.solicitudRepository = solicitudRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard() {
        List<Usuario> users = usuarioRepository.findAll();
        List<LocalEvento> venues = localRepository.findAllByOrderByCreadoEnDesc();
        List<Reserva> reservations = reservaRepository.findAllDetailed();

        return new AdminDashboardResponse(
                users.size(),
                countUsersByStatus(users, EstadoUsuario.ACTIVO),
                countUsersByStatus(users, EstadoUsuario.SUSPENDIDO),
                countUsersByStatus(users, EstadoUsuario.INACTIVO),
                countUsersByRole(users, Rol.CLIENTE),
                countUsersByRole(users, Rol.PROPIETARIO),
                venues.size(),
                venues.stream().filter(LocalEvento::isActivo).count(),
                venues.stream().filter(venue -> !venue.isActivo()).count(),
                reservations.size(),
                countByStatus(reservations, EstadoReserva.COMPLETADA),
                countByStatus(reservations, EstadoReserva.RECHAZADA),
                countByStatus(reservations, EstadoReserva.CANCELADA),
                sumApprovedTotal(reservations),
                sumApprovedServiceFee(reservations),
                resenaRepository.count(),
                solicitudRepository.countByEstado(EstadoSolicitudPropietario.PENDIENTE),
                monthlyMetrics(reservations),
                venueMetrics(venues, reservations).stream().limit(5).toList(),
                auditRepository.findTop10ByOrderByCreadoEnDesc().stream()
                        .map(ActivityItemResponse::from)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public OwnerDashboardResponse ownerDashboard(String subject) {
        UUID ownerId = parseSubject(subject);
        List<LocalEvento> venues = localRepository.findByPropietarioIdOrderByCreadoEnDesc(ownerId);
        List<Reserva> reservations = reservaRepository.findForOwner(ownerId);
        LocalDate today = LocalDate.now();

        List<Reserva> upcoming = reservations.stream()
                .filter(item -> item.getEstado() == EstadoReserva.COMPLETADA)
                .filter(item -> !item.getFecha().isBefore(today))
                .sorted(Comparator.comparing(Reserva::getFecha)
                        .thenComparing(Reserva::getHoraInicio))
                .toList();

        List<ReservationResponse> recentItems = reservations.stream()
                .limit(6)
                .map(ReservationResponse::from)
                .toList();

        List<ReservationResponse> upcomingItems = upcoming.stream()
                .limit(5)
                .map(ReservationResponse::from)
                .toList();

        return new OwnerDashboardResponse(
                venues.size(),
                venues.stream().filter(LocalEvento::isActivo).count(),
                reservations.size(),
                countByStatus(reservations, EstadoReserva.COMPLETADA),
                countByStatus(reservations, EstadoReserva.RECHAZADA),
                upcoming.size(),
                sumApprovedTotal(reservations),
                monthlyMetrics(reservations),
                venueMetrics(venues, reservations),
                recentItems,
                upcomingItems
        );
    }

    private long countUsersByStatus(List<Usuario> users, EstadoUsuario status) {
        return users.stream().filter(user -> user.getEstado() == status).count();
    }

    private long countUsersByRole(List<Usuario> users, Rol role) {
        return users.stream().filter(user -> user.getRoles().contains(role)).count();
    }

    private long countByStatus(List<Reserva> reservations, EstadoReserva status) {
        return reservations.stream().filter(item -> item.getEstado() == status).count();
    }

    private BigDecimal sumApprovedTotal(List<Reserva> reservations) {
        return reservations.stream()
                .filter(item -> item.getEstado() == EstadoReserva.COMPLETADA)
                .map(Reserva::getTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal sumApprovedServiceFee(List<Reserva> reservations) {
        return reservations.stream()
                .filter(item -> item.getEstado() == EstadoReserva.COMPLETADA)
                .map(Reserva::getTarifaServicio)
                .reduce(ZERO, BigDecimal::add);
    }

    private List<MonthlyMetricResponse> monthlyMetrics(List<Reserva> reservations) {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int index = 5; index >= 0; index--) {
            months.add(current.minusMonths(index));
        }

        Map<YearMonth, List<Reserva>> grouped = reservations.stream()
                .collect(Collectors.groupingBy(item -> YearMonth.from(item.getCreadoEn())));

        return months.stream()
                .map(month -> {
                    List<Reserva> items = grouped.getOrDefault(month, List.of());
                    BigDecimal revenue = sumApprovedTotal(items);
                    String monthName = month.getMonth()
                            .getDisplayName(TextStyle.SHORT, new Locale("es", "EC"));
                    String label = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                    return new MonthlyMetricResponse(
                            month.toString(),
                            label,
                            items.size(),
                            revenue
                    );
                })
                .toList();
    }

    private List<VenueMetricResponse> venueMetrics(
            List<LocalEvento> venues,
            List<Reserva> reservations
    ) {
        Map<UUID, List<Reserva>> byVenue = reservations.stream()
                .collect(Collectors.groupingBy(item -> item.getLocal().getId()));

        return venues.stream()
                .map(venue -> {
                    List<Reserva> items = byVenue.getOrDefault(venue.getId(), List.of());
                    return new VenueMetricResponse(
                            venue.getId(),
                            venue.getNombre(),
                            items.size(),
                            countByStatus(items, EstadoReserva.COMPLETADA),
                            countByStatus(items, EstadoReserva.RECHAZADA),
                            sumApprovedTotal(items),
                            venue.getCalificacion()
                    );
                })
                .sorted(Comparator
                        .comparing(VenueMetricResponse::revenue)
                        .thenComparing(VenueMetricResponse::reservations)
                        .reversed())
                .toList();
    }

    private UUID parseSubject(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El identificador del token no es válido.", exception);
        }
    }
}
