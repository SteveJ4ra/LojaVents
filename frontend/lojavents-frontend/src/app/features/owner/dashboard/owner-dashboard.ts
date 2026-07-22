import { CurrencyPipe } from '@angular/common';
import { Component, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { BookingService } from '../../../core/services/booking.service';
import { VenueService } from '../../../core/services/venue.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-owner-dashboard',
  standalone: true,
  imports: [CurrencyPipe, RouterLink, StatusBadge],
  templateUrl: './owner-dashboard.html',
  styleUrl: './owner-dashboard.scss'
})
export class OwnerDashboard {
  readonly ownerVenues = computed(() => {
    const id = this.auth.currentUser()?.id;
    return id ? this.venues.getByOwner(id) : [];
  });
  readonly ownerBookings = computed(() => {
    const ids = new Set(this.ownerVenues().map(venue => venue.id));
    return this.bookings.bookings().filter(booking => ids.has(booking.venueId));
  });
  readonly totalIncome = computed(() =>
    this.ownerBookings().filter(item => item.status === 'COMPLETADA').reduce((sum, item) => sum + item.total, 0)
  );

  constructor(
    private readonly auth: AuthService,
    readonly venues: VenueService,
    readonly bookings: BookingService
  ) {}

  venueName(id: string): string {
    return this.venues.getById(id)?.name ?? 'Local';
  }
}
