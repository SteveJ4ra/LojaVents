import { Component, computed } from '@angular/core';
import { Router } from '@angular/router';
import { FavoriteService } from '../../../core/services/favorite.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { VenueCard } from '../../../shared/components/venue-card/venue-card';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [VenueCard, EmptyState],
  templateUrl: './favorites.html',
  styleUrl: './favorites.scss'
})
export class Favorites {
  readonly venues = computed(() =>
    this.venueService.activeVenues().filter(venue => this.favorites.currentIds().includes(venue.id))
  );

  constructor(
    private readonly venueService: VenueService,
    readonly favorites: FavoriteService,
    private readonly notifications: NotificationService
  ) {}

  toggle(id: string): void {
    this.favorites.toggle(id);
    this.notifications.show('Local eliminado de favoritos.', 'success');
  }
}
