import { Injectable } from '@angular/core';
import { Review } from '../../shared/models/review.model';
import { MockStoreService } from './mock-store.service';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  constructor(private readonly store: MockStoreService) {}

  getByVenue(venueId: string): Review[] {
    return this.store.reviews()
      .filter(review => review.venueId === venueId)
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  }

  create(review: Review): void {
    this.store.reviews.update(items => [review, ...items]);
    this.store.persist();
  }
}
