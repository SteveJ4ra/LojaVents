import { Component, computed, effect, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CircleHelp, LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { FavoriteService } from '../../../core/services/favorite.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { VenueCard } from '../../../shared/components/venue-card/venue-card';
import { ecuadorToday } from '../../../shared/utils/date-time';

@Component({
  selector: 'app-venue-search',
  standalone: true,
  imports: [ReactiveFormsModule, VenueCard, EmptyState, LucideAngularModule],
  templateUrl: './venue-search.html',
  styleUrl: './venue-search.scss'
})
export class VenueSearch {
  readonly HelpIcon = CircleHelp;
  readonly minDate = ecuadorToday();
  readonly filters = signal({ eventType: '', date: '', attendees: 1, maxPrice: 0, text: '' });
  readonly form = this.fb.nonNullable.group(this.filters());
  readonly results = computed(() => this.venueService.search(this.filters()));

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    readonly venueService: VenueService,
    readonly favorites: FavoriteService,
    readonly auth: AuthService,
    private readonly notifications: NotificationService
  ) {
    const params = this.route.snapshot.queryParamMap;
    const initial = {
      eventType: params.get('eventType') ?? '',
      date: params.get('date') ?? '',
      attendees: Number(params.get('attendees') ?? 1),
      maxPrice: Number(params.get('maxPrice') ?? 0),
      text: params.get('text') ?? ''
    };
    this.form.setValue(initial);
    this.filters.set(initial);

    this.form.valueChanges.subscribe(value => {
      this.filters.set({ ...initial, ...value } as typeof initial);
    });

    this.venueService.ensurePublicVenues();
  }

  reset(): void {
    this.form.setValue({ eventType: '', date: '', attendees: 1, maxPrice: 0, text: '' });
  }

  reload(): void {
    this.venueService.loadPublicVenues().subscribe({ error: () => undefined });
  }

  toggleFavorite(id: string): void {
    if (this.auth.hasRole('ADMINISTRADOR')) return;
    if (!this.auth.isAuthenticated()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: '/locales' } });
      return;
    }
    this.favorites.toggle(id).subscribe({
      next: added => this.notifications.show(
        added ? 'Local guardado.' : 'Local eliminado.',
        'success'
      ),
      error: () => this.notifications.show('No fue posible actualizar el favorito.', 'error')
    });
  }
}
