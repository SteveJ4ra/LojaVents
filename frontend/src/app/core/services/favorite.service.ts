import { effect, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Venue } from '../../shared/models/venue.model';
import { AuthService } from './auth.service';

interface FavoriteStatusResponse {
  venueId: string;
  favorite: boolean;
}

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  private readonly apiUrl = `${environment.apiBaseUrl}/favoritos`;
  private readonly idsState = signal<string[]>([]);
  private readonly venuesState = signal<Venue[]>([]);
  private loadedUserId: string | null = null;

  readonly currentIds = this.idsState.asReadonly();
  readonly favoriteVenues = this.venuesState.asReadonly();

  constructor(
    private readonly http: HttpClient,
    private readonly auth: AuthService
  ) {
    effect(() => {
      const userId = this.auth.currentUser()?.id ?? null;
      if (!userId) {
        this.loadedUserId = null;
        this.idsState.set([]);
        this.venuesState.set([]);
        return;
      }
      if (this.loadedUserId !== userId) {
        this.loadedUserId = userId;
        this.loadIds().subscribe({ error: () => this.idsState.set([]) });
      }
    });
  }

  loadIds(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/ids`).pipe(
      tap(ids => this.idsState.set(ids))
    );
  }

  loadVenues(): Observable<Venue[]> {
    return this.http.get<Venue[]>(this.apiUrl).pipe(
      tap(venues => {
        this.venuesState.set(venues);
        this.idsState.set(venues.map(venue => venue.id));
      })
    );
  }

  has(venueId: string): boolean {
    return this.idsState().includes(venueId);
  }

  toggle(venueId: string): Observable<boolean> {
    const removing = this.has(venueId);
    const request = removing
      ? this.http.delete<FavoriteStatusResponse>(`${this.apiUrl}/${venueId}`)
      : this.http.post<FavoriteStatusResponse>(`${this.apiUrl}/${venueId}`, {});

    return request.pipe(
      tap(response => {
        this.idsState.update(ids => response.favorite
          ? [...new Set([...ids, venueId])]
          : ids.filter(id => id !== venueId)
        );
        if (!response.favorite) {
          this.venuesState.update(venues => venues.filter(venue => venue.id !== venueId));
        }
      }),
      map(response => response.favorite)
    );
  }
}
