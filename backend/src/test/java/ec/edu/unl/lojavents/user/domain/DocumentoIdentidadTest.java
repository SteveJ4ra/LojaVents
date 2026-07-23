package ec.edu.unl.lojavents.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoIdentidadTest {

    @Test
    void validatesEachDocumentTypeIndependently() {
        assertEquals("1100000000",
                new DocumentoIdentidad(TipoDocumentoIdentidad.CEDULA, "1100000000").getNumero());
        assertEquals("AB123456",
                new DocumentoIdentidad(TipoDocumentoIdentidad.PASAPORTE, "ab123456").getNumero());
        assertEquals("LIC12345",
                new DocumentoIdentidad(TipoDocumentoIdentidad.LICENCIA_CONDUCIR, "lic12345").getNumero());
    }

    @Test
    void rejectsFormatsThatDoNotMatchTheSelectedType() {
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentoIdentidad(TipoDocumentoIdentidad.CEDULA, "AB123456"));
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentoIdentidad(TipoDocumentoIdentidad.PASAPORTE, "123"));
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentoIdentidad(TipoDocumentoIdentidad.LICENCIA_CONDUCIR, "12.345"));
    }
}
