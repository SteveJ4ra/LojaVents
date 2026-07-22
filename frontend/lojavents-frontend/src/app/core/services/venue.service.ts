import { computed, Injectable } from '@angular/core';
import { AvailabilityBlock, Venue, VenueSearchFilters } from '../../shared/models/venue.model';
import { MockStoreService } from './mock-store.service';

@Injectable({ providedIn: 'root' })
export class VenueService {
  readonly venues = computed(() => this.store.venues());
  readonly activeVenues = computed(() => this.store.venues().filter(venue => venue.active));
  readonly featuredVenues = computed(() => this.activeVenues().filter(venue => venue.featured));

  constructor(private readonly store: MockStoreService) {}

  getById(id: string): Venue | undefined {
    return this.store.venues().find(venue => venue.id === id);
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
    return this.store.venues().filter(venue => venue.ownerId === ownerId);
  }

  save(venue: Venue): void {
    this.store.venues.update(items => {
      const index = items.findIndex(item => item.id === venue.id);
      return index === -1
        ? [...items, venue]
        : items.map(item => item.id === venue.id ? venue : item);
    });
    this.store.persist();
  }

  toggleActive(id: string): void {
    this.store.venues.update(items =>
      items.map(item => item.id === id ? { ...item, active: !item.active } : item)
    );
    this.store.persist();
  }

  addBlock(venueId: string, block: AvailabilityBlock): void {
    const venue = this.getById(venueId);
    if (!venue) return;
    this.save({ ...venue, blockedSlots: [...venue.blockedSlots, block] });
  }

  removeBlock(venueId: string, blockId: string): void {
    const venue = this.getById(venueId);
    if (!venue) return;
    this.save({
      ...venue,
      blockedSlots: venue.blockedSlots.filter(block => block.id !== blockId)
    });
  }

  eventTypes(): string[] {
    return [...new Set(this.store.venues().flatMap(venue => venue.eventTypes))].sort();
  }
}
