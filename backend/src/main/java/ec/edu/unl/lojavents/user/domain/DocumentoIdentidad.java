package ec.edu.unl.lojavents.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Locale;
import java.util.Objects;

@Embeddable
public class DocumentoIdentidad {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30)
    private TipoDocumentoIdentidad tipo;

    @Column(name = "identificacion", nullable = false, length = 30)
    private String numero;

    protected DocumentoIdentidad() {
    }

    public DocumentoIdentidad(TipoDocumentoIdentidad tipo, String numero) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de documento es obligatorio.");
        this.numero = normalize(numero);
        validate();
    }

    public TipoDocumentoIdentidad getTipo() {
        return tipo;
    }

    public String getNumero() {
        return numero;
    }

    private String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "El numero de documento es obligatorio.")
                .trim()
                .toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("El numero de documento es obligatorio.");
        }
        return normalized;
    }

    private void validate() {
        boolean valid = switch (tipo) {
            case CEDULA -> numero.matches("[0-9]{10}");
            case PASAPORTE -> numero.matches("[A-Z0-9]{6,20}");
            case LICENCIA_CONDUCIR -> numero.matches("[A-Z0-9]{5,20}");
        };
        if (!valid) {
            throw new IllegalArgumentException("El numero no es valido para el tipo de documento seleccionado.");
        }
    }
}
