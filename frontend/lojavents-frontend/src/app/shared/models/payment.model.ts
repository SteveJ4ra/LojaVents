export type PaymentSimulationMode = 'APPROVE' | 'REJECT';

export interface PaymentResult {
  success: boolean;
  reference?: string;
  message: string;
}
