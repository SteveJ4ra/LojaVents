import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateReviewRequest, Review } from '../../shared/models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly reviewsState = signal<Record<string, Review[]>>({});

  constructor(private readonly http: HttpClient) {}

  getByVenue(venueId: string): Review[] {
    return this.reviewsState()[venueId] ?? [];
  }

  loadByVenue(venueId: string): Observable<Review[]> {
    return this.http.get<Review[]>(
      `${environment.apiBaseUrl}/locales/${venueId}/resenas`
    ).pipe(
      tap(reviews => this.reviewsState.update(all => ({ ...all, [venueId]: reviews })))
    );
  }

  create(bookingId: string, request: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(
      `${environment.apiBaseUrl}/reservas/${bookingId}/resena`,
      request
    ).pipe(
      tap(review => this.reviewsState.update(all => ({
        ...all,
        [review.venueId]: [
          review,
          ...(all[review.venueId] ?? []).filter(item => item.id !== review.id)
        ]
      })))
    );
  }
}
