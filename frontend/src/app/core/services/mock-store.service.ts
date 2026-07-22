import { Injectable, signal } from '@angular/core';
import {
  MOCK_BOOKINGS,
  MOCK_CREDENTIALS,
  MOCK_OWNER_REQUESTS,
  MOCK_REVIEWS,
  MOCK_USERS,
  MOCK_VENUES
} from '../data/mock-data';
import { Booking } from '../../shared/models/booking.model';
import { Review } from '../../shared/models/review.model';
import { OwnerVerificationRequest, User } from '../../shared/models/user.model';
import { Venue } from '../../shared/models/venue.model';

interface PersistedState {
  users: User[];
  credentials: Record<string, string>;
  venues: Venue[];
  bookings: Booking[];
  reviews: Review[];
  ownerRequests: OwnerVerificationRequest[];
}

@Injectable({ providedIn: 'root' })
export class MockStoreService {
  private readonly storageKey = 'lojavents-mock-state-v1';
  private readonly initial = this.loadState();

  readonly users = signal<User[]>(this.initial.users);
  readonly credentials = signal<Record<string, string>>(this.initial.credentials);
  readonly venues = signal<Venue[]>(this.initial.venues);
  readonly bookings = signal<Booking[]>(this.initial.bookings);
  readonly reviews = signal<Review[]>(this.initial.reviews);
  readonly ownerRequests = signal<OwnerVerificationRequest[]>(this.initial.ownerRequests);

  persist(): void {
    const state: PersistedState = {
      users: this.users(),
      credentials: this.credentials(),
      venues: this.venues(),
      bookings: this.bookings(),
      reviews: this.reviews(),
      ownerRequests: this.ownerRequests()
    };
    localStorage.setItem(this.storageKey, JSON.stringify(state));
  }

  reset(): void {
    localStorage.removeItem(this.storageKey);
    this.users.set(structuredClone(MOCK_USERS));
    this.credentials.set(structuredClone(MOCK_CREDENTIALS));
    this.venues.set(structuredClone(MOCK_VENUES));
    this.bookings.set(structuredClone(MOCK_BOOKINGS));
    this.reviews.set(structuredClone(MOCK_REVIEWS));
    this.ownerRequests.set(structuredClone(MOCK_OWNER_REQUESTS));
    this.persist();
  }

  private loadState(): PersistedState {
    const fallback: PersistedState = {
      users: structuredClone(MOCK_USERS),
      credentials: structuredClone(MOCK_CREDENTIALS),
      venues: structuredClone(MOCK_VENUES),
      bookings: structuredClone(MOCK_BOOKINGS),
      reviews: structuredClone(MOCK_REVIEWS),
      ownerRequests: structuredClone(MOCK_OWNER_REQUESTS)
    };

    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) return fallback;
      return { ...fallback, ...JSON.parse(raw) } as PersistedState;
    } catch {
      localStorage.removeItem(this.storageKey);
      return fallback;
    }
  }
}
