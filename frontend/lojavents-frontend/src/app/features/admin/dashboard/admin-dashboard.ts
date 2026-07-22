import { Component, computed } from '@angular/core';
import { BookingService } from '../../../core/services/booking.service';
import { UserService } from '../../../core/services/user.service';
import { VenueService } from '../../../core/services/venue.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [StatusBadge],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard {
  readonly users = computed(() => this.userService.all());
  readonly pending = computed(() => this.userService.ownerRequests());

  constructor(
    private readonly userService: UserService,
    readonly venues: VenueService,
    readonly bookings: BookingService
  ) {}
}
