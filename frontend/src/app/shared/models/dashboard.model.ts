import { Booking } from './booking.model';

export interface MonthlyDashboardMetric {
  key: string;
  label: string;
  reservations: number;
  revenue: number;
}

export interface VenueDashboardMetric {
  venueId: string;
  venueName: string;
  reservations: number;
  completedReservations: number;
  rejectedReservations: number;
  revenue: number;
  rating: number;
}

export interface ActivityDashboardItem {
  id: string;
  type: string;
  actor: string;
  message: string;
  createdAt: string;
}

export interface AdminDashboardData {
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;
  inactiveUsers: number;
  clientUsers: number;
  ownerUsers: number;
  totalVenues: number;
  activeVenues: number;
  inactiveVenues: number;
  totalReservations: number;
  completedReservations: number;
  rejectedReservations: number;
  cancelledReservations: number;
  approvedRevenue: number;
  serviceFeeRevenue: number;
  totalReviews: number;
  pendingOwnerRequests: number;
  monthlyMetrics: MonthlyDashboardMetric[];
  topVenues: VenueDashboardMetric[];
  recentActivity: ActivityDashboardItem[];
}

export interface OwnerDashboardData {
  totalVenues: number;
  activeVenues: number;
  totalReservations: number;
  completedReservations: number;
  rejectedReservations: number;
  upcomingReservations: number;
  approvedRevenue: number;
  monthlyMetrics: MonthlyDashboardMetric[];
  venueMetrics: VenueDashboardMetric[];
  recentReservations: Booking[];
  upcomingReservationItems: Booking[];
}
