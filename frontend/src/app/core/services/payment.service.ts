import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Booking, PaymentSimulationMode } from '../../shared/models/booking.model';
import { BookingService } from './booking.service';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  constructor(private readonly bookings: BookingService) {}

  process(bookingId: string, mode: PaymentSimulationMode): Observable<Booking> {
    return this.bookings.processPayment(bookingId, mode);
  }
}
