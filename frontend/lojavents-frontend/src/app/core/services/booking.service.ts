import { computed, Injectable } from '@angular/core';
import { Booking, CreateBookingRequest } from '../../shared/models/booking.model';
import { MockStoreService } from './mock-store.service';

@Injectable({ providedIn: 'root' })
export class BookingService {
  readonly bookings = computed(() => this.store.bookings());

  constructor(private readonly store: MockStoreService) {}

  create(request: CreateBookingRequest): Booking {
    const booking: Booking = {
      ...request,
      id: `booking-${crypto.randomUUID()}`,
      createdAt: new Date().toISOString(),
      reviewSubmitted: false
    };
    this.store.bookings.update(items => [booking, ...items]);
    this.store.persist();
    return booking;
  }

  getByUser(userId: string): Booking[] {
    return this.store.bookings()
      .filter(booking => booking.userId === userId)
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  }

  getByVenue(venueId: string): Booking[] {
    return this.store.bookings().filter(booking => booking.venueId === venueId);
  }

  markReviewed(id: string): void {
    this.store.bookings.update(items =>
      items.map(item => item.id === id ? { ...item, reviewSubmitted: true } : item)
    );
    this.store.persist();
  }

  isSlotAvailable(venueId: string, date: string, startTime: string, durationHours: number): boolean {
    const start = this.toMinutes(startTime);
    const end = start + durationHours * 60;
    return !this.store.bookings().some(booking => {
      if (booking.venueId !== venueId || booking.date !== date || booking.status !== 'COMPLETADA') {
        return false;
      }
      const existingStart = this.toMinutes(booking.startTime);
      const existingEnd = existingStart + booking.durationHours * 60;
      return start < existingEnd && end > existingStart;
    });
  }

  private toMinutes(time: string): number {
    const [hours, minutes] = time.split(':').map(Number);
    return hours * 60 + minutes;
  }
}
