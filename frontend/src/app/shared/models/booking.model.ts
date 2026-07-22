export type BookingStatus = 'EN_PROCESO' | 'COMPLETADA' | 'RECHAZADA' | 'CANCELADA';
export type PaymentStatus = 'APROBADO' | 'RECHAZADO';
export type PaymentSimulationMode = 'APPROVE' | 'REJECT_FUNDS' | 'REJECT_PROVIDER' | 'REJECT_APPLICATION';

export interface BillingAddress {
  city: string;
  neighborhood: string;
  street: string;
}

export interface Booking {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  venueId: string;
  venueName: string;
  date: string;
  startTime: string;
  durationHours: number;
  attendees: number;
  billingAddress: BillingAddress;
  subtotal: number;
  serviceFee: number;
  total: number;
  status: BookingStatus;
  paymentStatus?: PaymentStatus;
  paymentMode?: PaymentSimulationMode;
  paymentReference?: string;
  paymentMessage?: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt: string;
  reviewSubmitted: boolean;
}

export interface CreateBookingRequest {
  venueId: string;
  date: string;
  startTime: string;
  durationHours: number;
  attendees: number;
  billingAddress: BillingAddress;
  acceptedRules: boolean;
  acceptedCancellation: boolean;
}

export interface AvailabilityResult {
  available: boolean;
  message: string;
}
