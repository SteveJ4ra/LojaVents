import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, finalize, Observable, retry, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AvailabilityBlockRequest,
  Venue,
  VenueSaveRequest,
  VenueSearchFilters
} from '../../shared/models/venue.model';

@Injectable({ providedIn: 'root' })
export class VenueService {
  private readonly apiUrl = `${environment.apiBaseUrl}/locales`;
  private readonly ownerApiUrl = `${environment.apiBaseUrl}/propietario/locales`;
  private readonly adminApiUrl = `${environment.apiBaseUrl}/admin/locales`;
  private readonly venuesState = signal<Venue[]>([]);
  private readonly loadingState = signal(false);
  private readonly loadErrorState = signal(false);

  readonly venues = this.venuesState.asReadonly();
  readonly loading = this.loadingState.asReadonly();
  readonly loadError = this.loadErrorState.asReadonly();
  readonly activeVenues = computed(() => this.venuesState().filter(venue => venue.active));
  readonly featuredVenues = computed(() =>
    this.activeVenues().filter(venue => venue.featured)
  );

  constructor(private readonly http: HttpClient) {
    this.ensurePublicVenues();
  }

  ensurePublicVenues(): void {
    if (this.loadingState() || this.venuesState().length > 0) {
      return;
    }

    this.loadPublicVenues().subscribe({ error: () => undefined });
  }

  loadPublicVenues(): Observable<Venue[]> {
    this.loadingState.set(true);
    this.loadErrorState.set(false);
    return this.http.get<Venue[]>(this.apiUrl).pipe(
      retry({ count: 3, delay: 1000 }),
      tap(items => {
        this.venuesState.set(items);
      }),
      catchError(error => {
        this.loadErrorState.set(true);
        return throwError(() => error);
      }),
      finalize(() => this.loadingState.set(false))
    );
  }

  loadPublicVenue(id: string): Observable<Venue> {
    return this.http.get<Venue>(`${this.apiUrl}/${id}`).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  loadOwnerVenues(): Observable<Venue[]> {
    return this.http.get<Venue[]>(this.ownerApiUrl).pipe(
      tap(items => this.upsertMany(items))
    );
  }

  loadOwnerVenue(id: string): Observable<Venue> {
    return this.http.get<Venue>(`${this.ownerApiUrl}/${id}`).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  loadAdminVenues(): Observable<Venue[]> {
    return this.http.get<Venue[]>(this.adminApiUrl).pipe(
      tap(items => this.upsertMany(items))
    );
  }

  getById(id: string): Venue | undefined {
    return this.venuesState().find(venue => venue.id === id);
  }

  search(filters: VenueSearchFilters): Venue[] {
    const text = filters.text?.trim().toLowerCase();
    return this.activeVenues().filter(venue => {
      const textMatch = !text || [
        venue.name,
        venue.shortDescription,
        venue.neighborhood,
        venue.address,
        ...venue.eventTypes
      ].some(value => value.toLowerCase().includes(text));
      const eventMatch = !filters.eventType || venue.eventTypes.includes(filters.eventType);
      const capacityMatch = !filters.attendees || venue.capacity >= filters.attendees;
      const priceMatch = !filters.maxPrice || venue.pricePerHour <= filters.maxPrice;
      const dateMatch = !filters.date || !venue.blockedSlots.some(block => block.date === filters.date);
      return textMatch && eventMatch && capacityMatch && priceMatch && dateMatch;
    });
  }

  getByOwner(ownerId: string): Venue[] {
    return this.venuesState().filter(venue => venue.ownerId === ownerId);
  }

  create(request: VenueSaveRequest): Observable<Venue> {
    return this.http.post<Venue>(this.ownerApiUrl, request).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  uploadImages(files: File[]): Observable<string[]> {
    const payload = new FormData();
    files.forEach(file => payload.append('files', file));
    return this.http.post<string[]>(`${environment.apiBaseUrl}/propietario/imagenes`, payload);
  }

  update(id: string, request: VenueSaveRequest): Observable<Venue> {
    return this.http.put<Venue>(`${this.ownerApiUrl}/${id}`, request).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  changeOwnerStatus(id: string, active: boolean): Observable<Venue> {
    return this.http.patch<Venue>(`${this.ownerApiUrl}/${id}/estado`, { active }).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  changeAdminStatus(id: string, active: boolean): Observable<Venue> {
    return this.http.patch<Venue>(`${this.adminApiUrl}/${id}/estado`, { active }).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  addBlock(venueId: string, request: AvailabilityBlockRequest): Observable<Venue> {
    return this.http.post<Venue>(`${this.ownerApiUrl}/${venueId}/bloqueos`, request).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  removeBlock(venueId: string, blockId: string): Observable<Venue> {
    return this.http.delete<Venue>(`${this.ownerApiUrl}/${venueId}/bloqueos/${blockId}`).pipe(
      tap(venue => this.upsert(venue))
    );
  }

  eventTypes(): string[] {
    return [...new Set(this.venuesState().flatMap(venue => venue.eventTypes))].sort();
  }

  private upsert(venue: Venue): void {
    this.venuesState.update(items => {
      const exists = items.some(item => item.id === venue.id);
      return exists
        ? items.map(item => item.id === venue.id ? venue : item)
        : [...items, venue];
    });
  }

  private upsertMany(venues: Venue[]): void {
    venues.forEach(venue => this.upsert(venue));
  }
}
