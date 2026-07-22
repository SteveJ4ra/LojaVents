import { CurrencyPipe } from '@angular/common';
import { Component } from '@angular/core';
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
}
