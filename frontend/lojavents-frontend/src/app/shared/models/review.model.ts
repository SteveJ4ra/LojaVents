export interface Review {
  id: string;
  bookingId: string;
  venueId: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}
