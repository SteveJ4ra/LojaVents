package ec.edu.unl.lojavents.venue.application;

import ec.edu.unl.lojavents.common.api.ApiException;
import org.springframework.http.HttpStatus;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class VenueCatalog {

    private static final List<String> EVENT_TYPES = List.of(
            "Bodas", "Cumpleaños", "Quinceaños", "Graduaciones", "Corporativo",
            "Eventos sociales", "Conferencias", "Talleres", "Reuniones", "Fotografía"
    );
    private static final List<String> AMENITIES = List.of(
            "Estacionamiento", "Wi-Fi", "Sonido", "Proyector", "Cocina",
            "Aire acondicionado", "Área verde", "Zona cubierta", "Mobiliario", "Iluminación"
    );

    private VenueCatalog() {
    }

    static Set<String> eventTypes(List<String> values) {
        return canonicalize(values, EVENT_TYPES, "tipo de evento");
    }

    static Set<String> amenities(List<String> values) {
        return canonicalize(values, AMENITIES, "amenidad");
    }

    private static Set<String> canonicalize(List<String> values, List<String> catalog, String label) {
        Map<String, String> options = new LinkedHashMap<>();
        catalog.forEach(value -> options.put(key(value), value));
        Set<String> result = new java.util.LinkedHashSet<>();
        for (String value : values) {
            String canonical = options.get(key(value));
            if (canonical == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VENUE_CATALOG_VALUE",
                        "Selecciona un " + label + " valido de las opciones disponibles.");
            }
            result.add(canonical);
        }
        return result;
    }

    private static String key(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
