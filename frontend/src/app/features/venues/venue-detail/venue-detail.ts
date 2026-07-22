import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { FavoriteService } from '../../../core/services/favorite.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ReviewService } from '../../../core/services/review.service';
import { ShareService } from '../../../core/services/share.service';
import { VenueService } from '../../../core/services/venue.service';

@Component({
  selector: 'app-venue-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './venue-detail.html',
  styleUrl: './venue-detail.scss'
})
export class VenueDetail {
  readonly venueId = this.route.snapshot.paramMap.get('id') ?? '';
  readonly venue = computed(() => this.venues.getById(this.venueId));
  readonly reviews = computed(() => this.reviewService.getByVenue(this.venueId));

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    readonly venues: VenueService,
    readonly favorites: FavoriteService,
    private readonly auth: AuthService,
    private readonly notifications: NotificationService,
    private readonly reviewService: ReviewService,
    private readonly shareService: ShareService
  ) {
    this.venues.loadPublicVenue(this.venueId).subscribe({
      error: () => undefined
    });
    this.reviewService.loadByVenue(this.venueId).subscribe({
      error: () => undefined
    });
  }

  favorite(): void {
    if (!this.auth.isAuthenticated()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    this.favorites.toggle(this.venueId).subscribe({
      next: added => this.notifications.show(
        added ? 'Local agregado a favoritos.' : 'Local eliminado de favoritos.',
        'success'
      ),
      error: () => this.notifications.show('No fue posible actualizar el favorito.', 'error')
    });
  }

  async share(): Promise<void> {
    const venue = this.venue();
    if (!venue) return;
    try {
      const result = await this.shareService.share(venue.name, window.location.href);
      this.notifications.show(result === 'copied' ? 'Enlace copiado.' : 'Enlace compartido.', 'success');
    } catch {
      this.notifications.show('No fue posible compartir el enlace.', 'error');
    }
  }
}
