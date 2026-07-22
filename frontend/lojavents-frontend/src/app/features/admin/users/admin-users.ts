import { Component, computed } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { User, UserStatus } from '../../../shared/models/user.model';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [StatusBadge],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsers {
  readonly users = computed(() => this.userService.all());

  constructor(
    private readonly userService: UserService,
    private readonly notifications: NotificationService
  ) {}

  changeStatus(user: User): void {
    const next: UserStatus = user.status === 'ACTIVO' ? 'SUSPENDIDO' : 'ACTIVO';
    this.userService.setStatus(user.id, next);
    this.notifications.show('Estado de la cuenta actualizado.', 'success');
  }
}
