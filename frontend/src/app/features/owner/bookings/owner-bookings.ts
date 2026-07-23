import { CurrencyPipe } from '@angular/common';
import { Component, computed } from '@angular/core';
import { BookingService } from '../../../core/services/booking.service';
import { NotificationService } from '../../../core/services/notification.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { formatEcuadorDate } from '../../../shared/utils/date-time';

@Component({
  selector: 'app-owner-bookings',
  standalone: true,
  imports: [CurrencyPipe, StatusBadge],
  templateUrl: './owner-bookings.html',
  styleUrl: './owner-bookings.scss'
})
export class OwnerBookings {
  readonly bookings = computed(() => this.bookingService.ownerBookings());
  readonly approvedIncome = computed(() =>
    this.bookings()
      .filter(item => item.status === 'CONFIRMADA')
      .reduce((sum, item) => sum + item.total, 0)
  );

  constructor(
    private readonly bookingService: BookingService,
    private readonly notifications: NotificationService
  ) {
    this.bookingService.loadOwner().subscribe({
      error: () => this.notifications.show('No fue posible cargar las reservas recibidas.', 'error')
    });
  }

  formatEventDate(value: string): string {
    return formatEcuadorDate(value);
  }
}
