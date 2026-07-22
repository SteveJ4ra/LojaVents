import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { BookingService } from '../../../core/services/booking.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ReviewService } from '../../../core/services/review.service';
import { VenueService } from '../../../core/services/venue.service';
import { Booking } from '../../../shared/models/booking.model';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, EmptyState, StatusBadge],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.scss'
})
export class MyBookings {
  readonly selected = signal<Booking | null>(null);
  readonly form = this.fb.nonNullable.group({
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['', [Validators.required, Validators.minLength(10)]]
  });
  readonly bookings = computed(() => {
    const id = this.auth.currentUser()?.id;
    return id ? this.bookingService.getByUser(id) : [];
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly bookingService: BookingService,
    private readonly reviewService: ReviewService,
    private readonly notifications: NotificationService,
    readonly venues: VenueService
  ) {}

  venueName(id: string): string {
    return this.venues.getById(id)?.name ?? 'Local no disponible';
  }

  canReview(booking: Booking): boolean {
    return booking.status === 'COMPLETADA'
      && booking.date < new Date().toISOString().slice(0, 10)
      && !booking.reviewSubmitted;
  }

  submitReview(): void {
    const booking = this.selected();
    const user = this.auth.currentUser();
    if (!booking || !user || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.reviewService.create({
      id: `review-${crypto.randomUUID()}`,
      bookingId: booking.id,
      venueId: booking.venueId,
      userId: user.id,
      userName: user.fullName,
      rating: value.rating,
      comment: value.comment,
      createdAt: new Date().toISOString()
    });
    this.bookingService.markReviewed(booking.id);
    this.selected.set(null);
    this.form.reset({ rating: 5, comment: '' });
    this.notifications.show('Reseña publicada correctamente.', 'success');
  }
}
