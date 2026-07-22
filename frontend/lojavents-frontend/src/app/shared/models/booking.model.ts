export type BookingStatus = 'EN_PROCESO' | 'COMPLETADA' | 'RECHAZADA' | 'CANCELADA';

export interface BillingAddress {
  city: string;
  neighborhood: string;
  street: string;
}

export interface Booking {
  id: string;
  userId: string;
  venueId: string;
  date: string;
  startTime: string;
  durationHours: number;
  attendees: number;
  billingAddress: BillingAddress;
  subtotal: number;
  serviceFee: number;
  total: number;
  status: BookingStatus;
  paymentReference?: string;
  createdAt: string;
  reviewSubmitted: boolean;
}

export type CreateBookingRequest = Omit<Booking, 'id' | 'createdAt' | 'reviewSubmitted'>;
