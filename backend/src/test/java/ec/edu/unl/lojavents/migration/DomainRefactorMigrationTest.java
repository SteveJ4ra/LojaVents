package ec.edu.unl.lojavents.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DomainRefactorMigrationTest {

    @Test
    void migratesHistoricalDataAndKeepsPostgresConstraints() throws SQLException {
        String url = System.getenv("TEST_POSTGRES_URL");
        String user = System.getenv("TEST_POSTGRES_USER");
        String password = System.getenv("TEST_POSTGRES_PASSWORD");
        assumeTrue(url != null && user != null && password != null,
                "La prueba requiere un PostgreSQL real configurado mediante TEST_POSTGRES_*.");

        Flyway baseline = flyway(url, user, password, MigrationVersion.fromVersion("9"));
        baseline.clean();
        baseline.migrate();

        UUID ownerId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID publishedVenueId = UUID.randomUUID();
        UUID pendingVenueId = UUID.randomUUID();
        UUID inactiveVenueId = UUID.randomUUID();
        UUID firstReservationId = UUID.randomUUID();
        UUID secondReservationId = UUID.randomUUID();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            insertUser(connection, ownerId, "owner-migration@lojavents.test");
            insertUser(connection, clientId, "client-migration@lojavents.test");
            insertVenue(connection, publishedVenueId, ownerId, "Publicado", true, false);
            insertVenue(connection, pendingVenueId, ownerId, "Pendiente", false, true);
            insertVenue(connection, inactiveVenueId, ownerId, "Inactivo", false, false);
            insertReservation(connection, firstReservationId, clientId, publishedVenueId, 1);
            insertReservation(connection, secondReservationId, clientId, publishedVenueId, 12);
        }

        flyway(url, user, password, null).migrate();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            assertEquals("PUBLICADO", venueStatus(connection, publishedVenueId));
            assertEquals("PENDIENTE_REVISION", venueStatus(connection, pendingVenueId));
            assertEquals("INACTIVO", venueStatus(connection, inactiveVenueId));
            assertLegacyPublicationFlags(connection, publishedVenueId, true, false);
            assertLegacyPublicationFlags(connection, pendingVenueId, false, true);
            assertLegacyPublicationFlags(connection, inactiveVenueId, false, false);

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE locales SET estado_publicacion = 'INACTIVO' WHERE id = ?")) {
                statement.setObject(1, publishedVenueId);
                statement.executeUpdate();
            }
            assertLegacyPublicationFlags(connection, publishedVenueId, false, false);

            Set<String> references = new HashSet<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, estado, referencia_publica FROM reservas ORDER BY id")) {
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        UUID id = rows.getObject("id", UUID.class);
                        String reference = rows.getString("referencia_publica");
                        assertEquals("CONFIRMADA", rows.getString("estado"));
                        assertTrue(reference.matches("LV-[A-F0-9]{32}"));
                        assertFalse(reference.contains(id.toString().replace("-", "").toUpperCase()));
                        assertTrue(references.add(reference));
                    }
                }
            }
            assertEquals(2, references.size());

            assertDurationAccepted(connection, clientId, publishedVenueId, 1);
            assertDurationAccepted(connection, clientId, publishedVenueId, 12);
            assertDurationRejected(connection, clientId, publishedVenueId, 0);
            assertDurationRejected(connection, clientId, publishedVenueId, 13);
        }
    }

    private Flyway flyway(String url, String user, String password, MigrationVersion target) {
        var configuration = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .cleanDisabled(false);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private void insertUser(Connection connection, UUID id, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO usuarios (id, nombres, email, password_hash, telefono)
                VALUES (?, 'Migration Test', ?, 'hash', '0999999999')
                """)) {
            statement.setObject(1, id);
            statement.setString(2, email);
            statement.executeUpdate();
        }
    }

    private void insertVenue(
            Connection connection,
            UUID id,
            UUID ownerId,
            String name,
            boolean active,
            boolean pending
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO locales (
                    id, propietario_id, nombre, descripcion_corta, descripcion,
                    sector, direccion, precio_hora, capacidad, activo,
                    pendiente_revision, politica_cancelacion
                ) VALUES (?, ?, ?, 'Breve', 'Descripcion', 'Centro', 'Calle principal',
                          40.00, 100, ?, ?, 'Sin devoluciones')
                """)) {
            statement.setObject(1, id);
            statement.setObject(2, ownerId);
            statement.setString(3, name);
            statement.setBoolean(4, active);
            statement.setBoolean(5, pending);
            statement.executeUpdate();
        }
    }

    private void insertReservation(
            Connection connection,
            UUID id,
            UUID clientId,
            UUID venueId,
            int duration
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO reservas (
                    id, cliente_id, local_id, fecha, hora_inicio, duracion_horas,
                    asistentes, ciudad_facturacion, sector_facturacion,
                    direccion_facturacion, subtotal, tarifa_servicio, total,
                    estado, reglas_aceptadas, cancelacion_aceptada
                ) VALUES (?, ?, ?, DATE '2030-08-20', TIME '16:00', ?, 20,
                          'Loja', 'Centro', 'Calle principal', 40.00, 3.20, 43.20,
                          'COMPLETADA', TRUE, TRUE)
                """)) {
            statement.setObject(1, id);
            statement.setObject(2, clientId);
            statement.setObject(3, venueId);
            statement.setInt(4, duration);
            statement.executeUpdate();
        }
    }

    private String venueStatus(Connection connection, UUID venueId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT estado_publicacion FROM locales WHERE id = ?")) {
            statement.setObject(1, venueId);
            try (ResultSet row = statement.executeQuery()) {
                assertTrue(row.next());
                return row.getString(1);
            }
        }
    }

    private void assertLegacyPublicationFlags(
            Connection connection,
            UUID venueId,
            boolean active,
            boolean pending
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT activo, pendiente_revision FROM locales WHERE id = ?")) {
            statement.setObject(1, venueId);
            try (ResultSet row = statement.executeQuery()) {
                assertTrue(row.next());
                assertEquals(active, row.getBoolean("activo"));
                assertEquals(pending, row.getBoolean("pendiente_revision"));
            }
        }
    }

    private void assertDurationAccepted(
            Connection connection,
            UUID clientId,
            UUID venueId,
            int duration
    ) throws SQLException {
        try (PreparedStatement statement = durationInsert(connection, clientId, venueId, duration)) {
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void assertDurationRejected(
            Connection connection,
            UUID clientId,
            UUID venueId,
            int duration
    ) throws SQLException {
        try (PreparedStatement statement = durationInsert(connection, clientId, venueId, duration)) {
            assertThrows(SQLException.class, statement::executeUpdate);
        }
        try (Statement rollback = connection.createStatement()) {
            rollback.execute("ROLLBACK");
        }
    }

    private PreparedStatement durationInsert(
            Connection connection,
            UUID clientId,
            UUID venueId,
            int duration
    ) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO reservas (
                    cliente_id, local_id, fecha, hora_inicio, duracion_horas,
                    asistentes, ciudad_facturacion, sector_facturacion,
                    direccion_facturacion, subtotal, tarifa_servicio, total,
                    estado, reglas_aceptadas, cancelacion_aceptada, referencia_publica
                ) VALUES (?, ?, DATE '2030-08-21', TIME '10:00', ?, 10,
                          'Loja', 'Centro', 'Calle secundaria', 20.00, 1.60, 21.60,
                          'EN_PROCESO', TRUE, TRUE, ?)
                """);
        statement.setObject(1, clientId);
        statement.setObject(2, venueId);
        statement.setInt(3, duration);
        statement.setString(4, "LV-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        return statement;
    }
}
