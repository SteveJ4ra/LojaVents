export interface AvailabilityBlock {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  reason: string;
}

export interface Venue {
  id: string;
  ownerId: string;
  name: string;
  shortDescription: string;
  description: string;
  neighborhood: string;
  address: string;
  pricePerHour: number;
  capacity: number;
  rating: number;
  reviewCount: number;
  eventTypes: string[];
  amenities: string[];
  rules: string[];
  cancellationPolicy: string;
  images: string[];
  featured: boolean;
  active: boolean;
  blockedSlots: AvailabilityBlock[];
}

export interface VenueSearchFilters {
  eventType?: string;
  date?: string;
  attendees?: number;
  maxPrice?: number;
  text?: string;
}
