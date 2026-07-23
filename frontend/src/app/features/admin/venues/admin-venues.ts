import { CurrencyPipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-admin-venues',
  standalone: true,
  imports: [CurrencyPipe, StatusBadge],
  templateUrl: './admin-venues.html',
  styleUrl: './admin-venues.scss'
})
export class AdminVenues {
  readonly query = signal('');
  readonly filteredVenues = computed(() => {
    const query = normalize(this.query());
    return [...this.venues.venues()]
      .filter(venue => !query || [
        venue.name, venue.neighborhood, venue.address, ...venue.eventTypes
      ].some(value => normalize(value).includes(query)))
      .sort((left, right) => Number(Boolean(right.pendingReview)) - Number(Boolean(left.pendingReview))
        || left.name.localeCompare(right.name, 'es'));
  });

  constructor(
    readonly venues: VenueService,
    private readonly notifications: NotificationService
  ) {
    this.venues.loadAdminVenues().subscribe({
      error: () => this.notifications.show('No fue posible cargar los locales.', 'error')
    });
  }

  toggle(id: string): void {
    const venue = this.venues.getById(id);
    if (!venue) return;
    this.venues.changeAdminStatus(id, !venue.active).subscribe({
      next: () => this.notifications.show(
        venue.active ? 'Local desactivado.' : 'Local aprobado y publicado.',
        'success'
      ),
      error: error => this.notifications.show(error?.error?.detail ?? 'No fue posible actualizar el estado.', 'error')
    });
  }

  updateQuery(event: Event): void {
    this.query.set((event.target as HTMLInputElement).value);
  }
}

function normalize(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toLowerCase();
}
