package ec.edu.unl.lojavents.reservation.api.dto;

import ec.edu.unl.lojavents.reservation.domain.ModoPagoSimulado;
import jakarta.validation.constraints.NotNull;

public record PaymentSimulationRequest(
        @NotNull ModoPagoSimulado mode
) {
}
