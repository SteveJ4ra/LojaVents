package ec.edu.unl.lojavents.engagement.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewApplicationServiceTest {

    @Test
    void enablesReviewOnlyAfterTheReservationEndTime() {
        LocalDate date = LocalDate.of(2026, 7, 22);
        LocalTime startTime = LocalTime.of(18, 0);

        assertFalse(ReviewApplicationService.eventHasFinished(
                date, startTime, 3, LocalDateTime.of(2026, 7, 22, 21, 0)
        ));
        assertTrue(ReviewApplicationService.eventHasFinished(
                date, startTime, 3, LocalDateTime.of(2026, 7, 22, 21, 1)
        ));
    }
}
