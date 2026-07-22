import { CurrencyPipe } from '@angular/common';
import { Component, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-owner-venues',
  standalone: true,
  imports: [CurrencyPipe, RouterLink, EmptyState, StatusBadge],
  templateUrl: './owner-venues.html',
  styleUrl: './owner-venues.scss'
})
export class OwnerVenues {
  readonly venues = computed(() => {
    const id = this.auth.currentUser()?.id;
    return id ? this.venueService.getByOwner(id) : [];
  });

  constructor(
    private readonly auth: AuthService,
    private readonly venueService: VenueService,
    private readonly notifications: NotificationService
  ) {}

  toggle(id: string): void {
    this.venueService.toggleActive(id);
    this.notifications.show('Estado del local actualizado.', 'success');
  }
}
