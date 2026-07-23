package ec.edu.unl.lojavents.reservation.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReferenciaPublicaReservaTest {

    @Test
    void generatesOpaqueUniqueReferences() {
        Set<String> references = new HashSet<>();
        for (int index = 0; index < 1_000; index++) {
            String value = ReferenciaPublicaReserva.generar().getValor();
            assertTrue(value.matches("LV-[A-F0-9]{32}"));
            assertTrue(references.add(value));
        }
    }
}
