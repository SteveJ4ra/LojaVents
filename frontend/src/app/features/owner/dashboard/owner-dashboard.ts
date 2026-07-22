import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../../core/services/dashboard.service';
import { NotificationService } from '../../../core/services/notification.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-owner-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, StatusBadge],
  templateUrl: './owner-dashboard.html',
  styleUrl: './owner-dashboard.scss'
})
export class OwnerDashboard {
  readonly dashboard = this.dashboards.owner;
  readonly loading = this.dashboards.ownerLoading;
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
    this.dashboards.loadOwner().subscribe({
      error: () => this.notifications.show(
        'No fue posible cargar las estadísticas del propietario.',
        'error'
      )
    });
  }

  revenueBar(value: number): number {
    return Math.max(4, Math.round((value / this.maxRevenue()) * 100));
  }
}
