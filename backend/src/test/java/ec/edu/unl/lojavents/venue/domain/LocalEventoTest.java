package ec.edu.unl.lojavents.venue.domain;

import ec.edu.unl.lojavents.user.domain.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalEventoTest {

    @Test
    void allowsOnlyTheApprovedPublicationFlow() {
        LocalEvento venue = new LocalEvento(new Usuario("Owner", "owner@example.com", "hash", "0999999999"));

        assertEquals(EstadoPublicacionLocal.INACTIVO, venue.getEstadoPublicacion());
        assertThrows(IllegalStateException.class, venue::aprobarRevision);

        venue.solicitarRevision();
        assertTrue(venue.isPendienteRevision());
        venue.aprobarRevision();
        assertTrue(venue.isActivo());
        venue.desactivar();
        assertEquals(EstadoPublicacionLocal.INACTIVO, venue.getEstadoPublicacion());
    }
}
