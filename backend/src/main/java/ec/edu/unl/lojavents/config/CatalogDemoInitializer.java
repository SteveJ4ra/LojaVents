package ec.edu.unl.lojavents.config;

import ec.edu.unl.lojavents.user.domain.Rol;
import ec.edu.unl.lojavents.user.domain.Usuario;
import ec.edu.unl.lojavents.user.repository.UsuarioRepository;
import ec.edu.unl.lojavents.venue.domain.LocalEvento;
import ec.edu.unl.lojavents.venue.repository.LocalEventoRepository;
import ec.edu.unl.lojavents.storage.MediaStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

@Configuration
public class CatalogDemoInitializer {
    private static final List<String> VENUES = List.of(
            "Casa del Valle", "Salon Cima Andina", "Terraza San Sebastian", "Hacienda La Tebaida", "Foro Puerta del Sol",
            "Jardin Los Cipreses", "Centro Cultural Pucara", "Mirador El Plateado", "Salon Gran Colombia", "Quinta La Argelia",
            "Auditorio Clodoveo Jaramillo", "Terraza El Pedestal", "Casa Jardin Zamora", "Salon La Castellana", "Hacienda El Capuli",
            "Galeria Creativa Loja", "Jardin Las Pitas", "Centro de Eventos Catamayo", "Terraza El Valle", "Salon Gran Victoria",
            "Quinta El Cisne", "Foro Universitario", "Casa Patrimonial Sucre", "Jardin Belisario", "Salon La Pradera",
            "Hacienda Malacatos", "Terraza Vilcabamba", "Centro Social La Banda", "Auditorio Municipal", "Quinta El Carmen",
            "Jardin Valle Hermoso", "Salon La Colina", "Casa de Eventos Santiago", "Terraza El Panecillo", "Hacienda Rumishitana",
            "Foro Las Palmas", "Jardin El Rosal", "Centro de Convenciones Loja", "Salon Los Arupos", "Quinta San Cayetano",
            "Terraza La Inmaculada", "Casa Cultural El Sagrario", "Jardin La Florida", "Salon Montecristi", "Hacienda La Victoria",
            "Foro del Parque", "Jardin La Rinconada", "Centro Social La Alborada", "Salon El Descanso", "Quinta Los Nogales"
    );

    @Bean
    CommandLineRunner cargarCatalogoAmpliado(
            UsuarioRepository users,
            LocalEventoRepository venues,
            PasswordEncoder encoder,
            MediaStorageService media,
            PlatformTransactionManager transactionManager
    ) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return args -> transaction.executeWithoutResult(status -> {
            List<Usuario> owners = java.util.stream.IntStream.rangeClosed(1, 10)
                    .mapToObj(index -> users.findByEmailIgnoreCase("anfitrion" + index + "@lojavents.ec").orElseGet(() -> {
                        Usuario user = new Usuario("Anfitrion " + index + " LojaVents", "anfitrion" + index + "@lojavents.ec", encoder.encode("LojaVents2026"), "09900000" + String.format("%02d", index));
                        user.agregarRol(Rol.PROPIETARIO);
                        return users.save(user);
                    })).toList();
            java.util.stream.IntStream.rangeClosed(1, 12).forEach(index -> users.findByEmailIgnoreCase("cliente" + index + "@lojavents.ec").orElseGet(() -> users.save(new Usuario("Cliente " + index + " LojaVents", "cliente" + index + "@lojavents.ec", encoder.encode("LojaVents2026"), "09800000" + String.format("%02d", index)))));
            for (int index = 0; index < VENUES.size() - 3; index++) {
                String name = VENUES.get(index);
                if (venues.findAll().stream().anyMatch(venue -> venue.getNombre().equalsIgnoreCase(name))) continue;
                LocalEvento venue = new LocalEvento(owners.get(index % owners.size()));
                String type = switch (index % 5) { case 0 -> "Bodas"; case 1 -> "Quinceaños"; case 2 -> "Graduaciones"; case 3 -> "Corporativo"; default -> "Cumpleaños"; };
                venue.actualizar(name, "Espacio para " + type.toLowerCase() + " y celebraciones en Loja.", "Local equipado para eventos sociales, familiares y corporativos.", "Loja", "Sector " + (index + 1) + ", Loja", BigDecimal.valueOf(30 + (index % 9) * 8L), 40 + (index % 8) * 25, new LinkedHashSet<>(List.of(type, "Eventos sociales")), new LinkedHashSet<>(List.of("Estacionamiento", "Wi-Fi", "Sonido")), List.of("Respetar el aforo", "Cuidar las instalaciones"), "Consulta las condiciones de cancelacion antes de reservar.", List.of(imageUrl(media, index)));
                venue.configurarDestacado(index < 8);
                venue.solicitarRevision();
                venue.aprobarRevision();
                venues.save(venue);
            }
            List<LocalEvento> allVenues = venues.findAllByOrderByCreadoEnDesc();
            for (int index = 0; index < allVenues.size(); index++) {
                LocalEvento venue = allVenues.get(index);
                boolean needsStoredImage = venue.getImagenes().isEmpty()
                        || venue.getImagenes().stream().anyMatch(image -> image.startsWith("/images/"));
                if (!needsStoredImage) continue;
                venue.actualizar(
                        venue.getNombre(), venue.getDescripcionCorta(), venue.getDescripcion(), venue.getSector(), venue.getDireccion(),
                        venue.getPrecioHora(), venue.getCapacidad(), new LinkedHashSet<>(venue.getTiposEvento()),
                        new LinkedHashSet<>(venue.getAmenidades()), List.copyOf(venue.getReglas()), venue.getPoliticaCancelacion(),
                        List.of(imageUrl(media, index))
                );
                venues.save(venue);
            }
        });
    }

    private String imageUrl(MediaStorageService media, int index) {
        String name = "demo-images/venue-%02d.png".formatted((index % 50) + 1);
        try (var input = new ClassPathResource(name).getInputStream()) {
            return "/api/v1/imagenes/" + media.storeSeedImage("catalog-" + ((index % 50) + 1), name, "image/png", input);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("No fue posible cargar la imagen inicial del catalogo.", exception);
        }
    }
}
