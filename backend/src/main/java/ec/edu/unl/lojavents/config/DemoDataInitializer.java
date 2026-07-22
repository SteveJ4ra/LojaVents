package ec.edu.unl.lojavents.config;

import ec.edu.unl.lojavents.audit.domain.AuditEvent;
import ec.edu.unl.lojavents.audit.repository.AuditEventRepository;
import ec.edu.unl.lojavents.reservation.domain.*;
import ec.edu.unl.lojavents.reservation.repository.ReservaRepository;
import ec.edu.unl.lojavents.user.domain.Rol;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Configuration
public class DemoDataInitializer {

    private static final ZoneId LOJA_ZONE = ZoneId.of("America/Guayaquil");
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.08");

    @Bean
    CommandLineRunner cargarDatosDemo(
            UsuarioRepository usuarioRepository,
            LocalEventoRepository localRepository,
            ReservaRepository reservaRepository,
            AuditEventRepository auditEventRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Usuario admin = usuarioRepository.findByEmailIgnoreCase("admin@lojavents.ec")
                    .orElseGet(() -> {
                        Usuario user = new Usuario(
                                "Administrador LojaVents",
                                "admin@lojavents.ec",
                                passwordEncoder.encode("123456"),
                                "0999999999"
                        );
                        user.agregarRol(Rol.ADMINISTRADOR);
                        return usuarioRepository.save(user);
                    });

            Usuario cliente = usuarioRepository.findByEmailIgnoreCase("cliente@lojavents.ec")
                    .orElseGet(() -> usuarioRepository.save(new Usuario(
                            "Cliente LojaVents",
                            "cliente@lojavents.ec",
                            passwordEncoder.encode("123456"),
                            "0988888888"
                    )));

            Usuario propietario = usuarioRepository.findByEmailIgnoreCase("propietario@lojavents.ec")
                    .orElseGet(() -> {
                        Usuario user = new Usuario(
                                "Propietario LojaVents",
                                "propietario@lojavents.ec",
                                passwordEncoder.encode("123456"),
                                "0977777777"
                        );
                        user.agregarRol(Rol.PROPIETARIO);
                        return usuarioRepository.save(user);
                    });

            if (localRepository.count() == 0) {
                localRepository.save(createVenue(
                        propietario,
                        "Jardín Mirador del Valle",
                        "Jardín amplio con vista panorámica de Loja.",
                        "Un espacio al aire libre para celebraciones familiares, sesiones fotográficas y eventos sociales. Cuenta con áreas verdes, zona cubierta y acceso vehicular.",
                        "El Valle",
                        "Av. Salvador Bustamante Celi, sector El Valle",
                        "48.00",
                        90,
                        List.of("Bodas", "Cumpleaños", "Fotografía"),
                        List.of("Estacionamiento", "Área verde", "Zona cubierta", "Wi-Fi"),
                        List.of("No se permite fumar en zonas cubiertas", "La música debe finalizar a las 23:00"),
                        "Cancelación gratuita hasta 72 horas antes del evento.",
                        List.of("/images/venue-1.svg", "/images/venue-1b.svg"),
                        true
                ));

                localRepository.save(createVenue(
                        propietario,
                        "Salón Terra Nova",
                        "Salón moderno para reuniones y eventos corporativos.",
                        "Salón climatizado con mobiliario modular, proyector y cocina auxiliar. Adecuado para capacitaciones, lanzamientos, reuniones y celebraciones privadas.",
                        "La Argelia",
                        "Av. Pío Jaramillo Alvarado, La Argelia",
                        "62.00",
                        120,
                        List.of("Corporativo", "Graduaciones", "Conferencias"),
                        List.of("Proyector", "Sonido", "Cocina", "Aire acondicionado"),
                        List.of("No pegar decoración en las paredes", "Respetar el aforo máximo"),
                        "Se devuelve el 80% si la cancelación se realiza con cinco días de anticipación.",
                        List.of("/images/venue-2.svg", "/images/venue-2b.svg"),
                        true
                ));

                localRepository.save(createVenue(
                        propietario,
                        "Estudio Creativo Centro",
                        "Espacio íntimo para talleres, contenido y reuniones.",
                        "Estudio flexible en el centro de Loja, con iluminación, fondos fotográficos y mesas de trabajo. Ideal para talleres pequeños y creación de contenido.",
                        "Centro",
                        "Calle Bolívar y Azuay, centro de Loja",
                        "28.00",
                        35,
                        List.of("Talleres", "Fotografía", "Reuniones"),
                        List.of("Iluminación", "Fondos", "Wi-Fi", "Cafetera"),
                        List.of("Cuidar los equipos del estudio", "No ingresar alimentos al área fotográfica"),
                        "Cancelación gratuita hasta 24 horas antes.",
                        List.of("/images/venue-3.svg", "/images/venue-3b.svg"),
                        false
                ));
            }

            createReviewableReservationIfNeeded(cliente, localRepository, reservaRepository);

            if (auditEventRepository.count() == 0) {
                auditEventRepository.save(new AuditEvent(
                        "SISTEMA_INICIADO",
                        admin.getId().toString(),
                        "LojaVents inició correctamente con datos persistentes.",
                        Map.of("version", "favoritos-resenas-5")
                ));
            }
        };
    }

    private void createReviewableReservationIfNeeded(
            Usuario client,
            LocalEventoRepository localRepository,
            ReservaRepository reservaRepository
    ) {
        LocalDate today = LocalDate.now(LOJA_ZONE);
        boolean alreadyExists = reservaRepository.existsByCliente_IdAndEstadoAndFechaBefore(
                client.getId(),
                EstadoReserva.COMPLETADA,
                today
        );
        if (alreadyExists) {
            return;
        }

        localRepository.findByActivoTrueOrderByDestacadoDescNombreAsc().stream()
                .findFirst()
                .ifPresent(venue -> {
                    int duration = 4;
                    BigDecimal subtotal = venue.getPrecioHora()
                            .multiply(BigDecimal.valueOf(duration))
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal fee = subtotal.multiply(SERVICE_FEE_RATE)
                            .setScale(2, RoundingMode.HALF_UP);

                    Reserva reservation = new Reserva(
                            client,
                            venue,
                            today.minusDays(14),
                            LocalTime.of(17, 0),
                            duration,
                            Math.min(25, venue.getCapacidad()),
                            "Loja",
                            "Centro",
                            "Dirección registrada",
                            subtotal,
                            fee,
                            subtotal.add(fee),
                            true,
                            true
                    );
                    reservation.aprobarPago(new PagoSimulado(
                            EstadoPagoSimulado.APROBADO,
                            ModoPagoSimulado.APPROVE,
                            "SIM-DEMO-PAST-001",
                            "Pago aprobado para habilitar una reseña inicial."
                    ));
                    reservaRepository.save(reservation);
                });
    }

    private LocalEvento createVenue(
            Usuario owner,
            String name,
            String shortDescription,
            String description,
            String neighborhood,
            String address,
            String price,
            int capacity,
            List<String> eventTypes,
            List<String> amenities,
            List<String> rules,
            String cancellationPolicy,
            List<String> images,
            boolean featured
    ) {
        LocalEvento local = new LocalEvento(owner);
        local.actualizar(
                name,
                shortDescription,
                description,
                neighborhood,
                address,
                new BigDecimal(price),
                capacity,
                new LinkedHashSet<>(eventTypes),
                new LinkedHashSet<>(amenities),
                rules,
                cancellationPolicy,
                images
        );
        local.configurarDestacado(featured);
        local.configurarCalificacion(BigDecimal.ZERO, 0);
        return local;
    }
}
