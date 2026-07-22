import { Injectable } from '@angular/core';
import { PaymentResult, PaymentSimulationMode } from '../../shared/models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  async process(mode: PaymentSimulationMode): Promise<PaymentResult> {
    await new Promise(resolve => window.setTimeout(resolve, 900));
    if (mode === 'REJECT') {
      return {
        success: false,
        message: 'El pago simulado fue rechazado. No se realizó ningún cobro.'
      };
    }
    return {
      success: true,
      reference: `SIM-${Date.now()}`,
      message: 'Pago simulado aprobado.'
    };
  }
}
