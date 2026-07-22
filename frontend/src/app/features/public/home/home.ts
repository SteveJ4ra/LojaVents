import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { FavoriteService } from '../../../core/services/favorite.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { VenueCard } from '../../../shared/components/venue-card/venue-card';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, VenueCard],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {
  readonly searchForm = this.fb.nonNullable.group({
    eventType: [''],
    date: [''],
    attendees: [1]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    readonly venues: VenueService,
    readonly favorites: FavoriteService,
    private readonly auth: AuthService,
    private readonly notifications: NotificationService
  ) {
    this.venues.ensurePublicVenues();
  }

  search(): void {
    const value = this.searchForm.getRawValue();
    void this.router.navigate(['/locales'], { queryParams: value });
  }

  toggleFavorite(id: string): void {
    if (!this.auth.isAuthenticated()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: '/' } });
      return;
    }
    this.favorites.toggle(id).subscribe({
      next: added => this.notifications.show(
        added ? 'Local guardado en favoritos.' : 'Local eliminado de favoritos.',
        'success'
      ),
      error: () => this.notifications.show('No fue posible actualizar el favorito.', 'error')
    });
  }
}
