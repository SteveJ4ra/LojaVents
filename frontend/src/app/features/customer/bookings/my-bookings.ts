import { CurrencyPipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/services/booking.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ReviewService } from '../../../core/services/review.service';
import { VenueService } from '../../../core/services/venue.service';
import { Booking } from '../../../shared/models/booking.model';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { formatEcuadorDate, formatEcuadorDateTime, reservationHasFinished } from '../../../shared/utils/date-time';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink, EmptyState, StatusBadge],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.scss'
})
export class MyBookings {
  readonly selected = signal<Booking | null>(null);
  readonly details = signal<Booking | null>(null);
  readonly submittingReview = signal(false);
  readonly form = this.fb.nonNullable.group({
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]]
  });
  readonly bookings = computed(() => this.bookingService.bookings());

  constructor(
    private readonly fb: FormBuilder,
    private readonly bookingService: BookingService,
    private readonly reviewService: ReviewService,
    private readonly notifications: NotificationService,
    readonly venues: VenueService
  ) {
    this.bookingService.loadMine().subscribe({
      error: () => this.notifications.show('No fue posible cargar tus reservas.', 'error')
    });
  }

  venueName(booking: Booking): string {
    return booking.venueName || this.venues.getById(booking.venueId)?.name || 'Local no disponible';
  }

  canReview(booking: Booking): boolean {
    return booking.status === 'CONFIRMADA'
      && reservationHasFinished(booking.date, booking.startTime, booking.durationHours)
      && !booking.reviewSubmitted;
  }

  reviewWillBeAvailable(booking: Booking): boolean {
    return booking.status === 'CONFIRMADA'
      && !booking.reviewSubmitted
      && !reservationHasFinished(booking.date, booking.startTime, booking.durationHours);
  }

  formatEventDate(value: string): string {
    return formatEcuadorDate(value);
  }

  formatDateTime(value: string): string {
    return formatEcuadorDateTime(value);
  }

  submitReview(): void {
    const booking = this.selected();
    if (!booking || this.form.invalid || this.submittingReview()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.submittingReview.set(true);
    this.reviewService.create(booking.id, {
      rating: Number(value.rating),
      comment: value.comment.trim()
    }).subscribe({
      next: () => {
        this.bookingService.markReviewed(booking.id);
        this.venues.loadPublicVenue(booking.venueId).subscribe({ error: () => undefined });
        this.selected.set(null);
        this.form.reset({ rating: 5, comment: '' });
        this.submittingReview.set(false);
        this.notifications.show('Reseña publicada correctamente.', 'success');
      },
      error: error => {
        this.submittingReview.set(false);
        this.notifications.show(
          error?.error?.detail ?? 'No fue posible publicar la reseña.',
          'error'
        );
      }
    });
  }
}
