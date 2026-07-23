package ec.edu.unl.lojavents.reservation.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateReservationRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsOneAndTwelveHours() {
        assertTrue(validator.validate(request(1)).isEmpty());
        assertTrue(validator.validate(request(12)).isEmpty());
    }

    @Test
    void rejectsZeroAndThirteenHours() {
        assertFalse(validator.validate(request(0)).isEmpty());
        assertFalse(validator.validate(request(13)).isEmpty());
    }

    private CreateReservationRequest request(int duration) {
        return new CreateReservationRequest(
                UUID.randomUUID(),
                LocalDate.of(2030, 8, 20),
                LocalTime.of(16, 0),
                duration,
                20,
                new BillingAddressRequest("Loja", "Centro", "Main street"),
                true,
                true
        );
    }
}
