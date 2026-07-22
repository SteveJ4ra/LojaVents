import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../../core/services/dashboard.service';
import { NotificationService } from '../../../core/services/notification.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, StatusBadge],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard {
  readonly dashboard = this.dashboards.admin;
  readonly loading = this.dashboards.adminLoading;
  readonly maxReservations = computed(() =>
    Math.max(1, ...(this.dashboard()?.monthlyMetrics.map(item => item.reservations) ?? [1]))
  );
  readonly maxRevenue = computed(() =>
    Math.max(1, ...(this.dashboard()?.monthlyMetrics.map(item => item.revenue) ?? [1]))
  );

  constructor(
    private readonly dashboards: DashboardService,
    private readonly notifications: NotificationService
  ) {
    this.refresh();
  }

  refresh(): void {
    this.dashboards.loadAdmin().subscribe({
      error: () => this.notifications.show(
        'No fue posible cargar las estadísticas administrativas.',
        'error'
      )
    });
  }

  reservationBar(value: number): number {
    return Math.max(4, Math.round((value / this.maxReservations()) * 100));
  }

  revenueBar(value: number): number {
    return Math.max(4, Math.round((value / this.maxRevenue()) * 100));
  }

  activityLabel(type: string): string {
    return type.toLowerCase().replaceAll('_', ' ');
  }
}
