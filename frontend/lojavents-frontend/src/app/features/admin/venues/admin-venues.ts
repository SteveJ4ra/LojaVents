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
  ) {}

  toggle(id: string): void {
    this.venues.toggleActive(id);
    this.notifications.show('Estado del local actualizado.', 'success');
  }
}
