import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AvailabilityResult,
  Booking,
  CreateBookingRequest,
  PaymentSimulationMode
} from '../../shared/models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly apiUrl = `${environment.apiBaseUrl}/reservas`;
  private readonly ownerApiUrl = `${environment.apiBaseUrl}/propietario/reservas`;
  private readonly bookingsState = signal<Booking[]>([]);
  private readonly ownerBookingsState = signal<Booking[]>([]);
  private readonly loadingState = signal(false);

  readonly bookings = this.bookingsState.asReadonly();
  readonly ownerBookings = this.ownerBookingsState.asReadonly();
  readonly loading = this.loadingState.asReadonly();

  constructor(private readonly http: HttpClient) {}

  loadMine(): Observable<Booking[]> {
    this.loadingState.set(true);
    return this.http.get<Booking[]>(`${this.apiUrl}/mias`).pipe(
      tap(items => {
        this.bookingsState.set(items);
        this.loadingState.set(false);
      })
    );
  }

  loadOwner(): Observable<Booking[]> {
    this.loadingState.set(true);
    return this.http.get<Booking[]>(this.ownerApiUrl).pipe(
      tap(items => {
        this.ownerBookingsState.set(items);
        this.loadingState.set(false);
      })
    );
  }

  create(request: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.apiUrl, request).pipe(
      tap(booking => this.upsertMine(booking))
    );
  }

  processPayment(id: string, mode: PaymentSimulationMode): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/${id}/pago-simulado`, { mode }).pipe(
      tap(booking => this.upsertMine(booking))
    );
  }

  checkAvailability(
    venueId: string,
    date: string,
    startTime: string,
    durationHours: number
  ): Observable<AvailabilityResult> {
    const params = new HttpParams()
      .set('date', date)
      .set('startTime', startTime)
      .set('durationHours', durationHours);
    return this.http.get<AvailabilityResult>(
      `${environment.apiBaseUrl}/locales/${venueId}/disponibilidad`,
      { params }
    );
  }

  getByUser(userId: string): Booking[] {
    return this.bookingsState().filter(booking => booking.userId === userId);
  }

  getByVenue(venueId: string): Booking[] {
    return [...this.bookingsState(), ...this.ownerBookingsState()]
      .filter(booking => booking.venueId === venueId);
  }

  markReviewed(id: string): void {
    this.bookingsState.update(items =>
      items.map(item => item.id === id ? { ...item, reviewSubmitted: true } : item)
    );
  }

  private upsertMine(booking: Booking): void {
    this.bookingsState.update(items => {
      const exists = items.some(item => item.id === booking.id);
      return exists
        ? items.map(item => item.id === booking.id ? booking : item)
        : [booking, ...items];
    });
  }
}
