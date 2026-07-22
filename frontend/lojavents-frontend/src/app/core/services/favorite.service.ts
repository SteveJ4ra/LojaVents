import { computed, Injectable, signal } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  private readonly allFavorites = signal<Record<string, string[]>>(this.load());

  readonly currentIds = computed(() => {
    const userId = this.auth.currentUser()?.id;
    return userId ? this.allFavorites()[userId] ?? [] : [];
  });

  constructor(private readonly auth: AuthService) {}

  has(venueId: string): boolean {
    return this.currentIds().includes(venueId);
  }

  toggle(venueId: string): boolean {
    const userId = this.auth.currentUser()?.id;
    if (!userId) return false;

    const current = this.allFavorites()[userId] ?? [];
    const next = current.includes(venueId)
      ? current.filter(id => id !== venueId)
      : [...current, venueId];

    this.allFavorites.update(all => ({ ...all, [userId]: next }));
    localStorage.setItem('lojavents-favorites-v1', JSON.stringify(this.allFavorites()));
    return next.includes(venueId);
  }

  private load(): Record<string, string[]> {
    try {
      return JSON.parse(localStorage.getItem('lojavents-favorites-v1') ?? '{}');
    } catch {
      return {};
    }
  }
}
