import { Component } from '@angular/core';
import { FavoriteService } from '../../../core/services/favorite.service';
import { NotificationService } from '../../../core/services/notification.service';
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
  readonly venues = this.favorites.favoriteVenues;

  constructor(
    readonly favorites: FavoriteService,
    private readonly notifications: NotificationService
  ) {
    this.favorites.loadVenues().subscribe({
      error: () => this.notifications.show('No fue posible cargar tus favoritos.', 'error')
    });
  }

  toggle(id: string): void {
    this.favorites.toggle(id).subscribe({
      next: () => this.notifications.show('Local eliminado de favoritos.', 'success'),
      error: () => this.notifications.show('No fue posible eliminar el favorito.', 'error')
    });
  }
}
