import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { User, UserStatus } from '../../../shared/models/user.model';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { formatEcuadorDateTime } from '../../../shared/utils/date-time';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [StatusBadge],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsers implements OnInit {
  readonly query = signal('');
  readonly users = computed(() => {
    const query = normalize(this.query());
    return this.userService.all().filter(user => !query || [
      user.fullName, user.email, user.phone, ...user.roles
    ].some(value => normalize(value).includes(query)));
  });
  readonly loading = signal(true);
  readonly changingId = signal<string | null>(null);

  constructor(
    private readonly userService: UserService,
    private readonly notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.userService.loadUsers().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  changeStatus(user: User): void {
    const next: UserStatus = user.status === 'ACTIVO' ? 'SUSPENDIDO' : 'ACTIVO';
    this.changingId.set(user.id);
    this.userService.setStatus(user.id, next).pipe(
      finalize(() => this.changingId.set(null))
    ).subscribe({
      next: () => this.notifications.show('Estado de la cuenta actualizado.', 'success'),
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  updateQuery(event: Event): void {
    this.query.set((event.target as HTMLInputElement).value);
  }

  formatDateTime(value: string): string {
    return formatEcuadorDateTime(value);
  }

  private readError(error: unknown): string {
    const httpError = error as HttpErrorResponse;
    return httpError.error?.detail ?? 'No fue posible completar la operación.';
  }
}

function normalize(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toLowerCase();
}
