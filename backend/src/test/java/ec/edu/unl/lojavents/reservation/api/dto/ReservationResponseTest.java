package ec.edu.unl.lojavents.reservation.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationResponseTest {

    @Test
    void hidesLegacySimulationPrefixFromPaymentReferences() {
        assertEquals(
                "PAY-OK-20260720131730-9DFF8D88",
                ReservationResponse.publicPaymentReference("SIM-OK-20260720131730-9DFF8D88")
        );
        assertEquals(
                "PAY-DEMO-PAST-001",
                ReservationResponse.publicPaymentReference("SIM-DEMO-PAST-001")
        );
    }

    @Test
    void hidesLegacySimulationWordingFromRejectionReasons() {
        assertEquals(
                "El pago fue rechazado.",
                ReservationResponse.publicRejectionReason("Pago simulado rechazado.")
        );
    }
}
