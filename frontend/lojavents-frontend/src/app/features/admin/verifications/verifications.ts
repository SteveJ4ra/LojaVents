import { Component, computed } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-verifications',
  standalone: true,
  imports: [EmptyState],
  templateUrl: './verifications.html',
  styleUrl: './verifications.scss'
})
export class Verifications {
  readonly requests = computed(() => this.users.ownerRequests());

  constructor(
    private readonly users: UserService,
    private readonly notifications: NotificationService,
    private readonly auth: AuthService
  ) {}

  userName(id: string): string {
    return this.users.all().find(user => user.id === id)?.fullName ?? id;
  }

  approve(id: string): void {
    this.users.approveOwner(id);
    this.auth.refreshSession();
    this.notifications.show('Rol de propietario aprobado.', 'success');
  }

  reject(id: string): void {
    this.users.rejectOwner(id);
    this.notifications.show('Solicitud rechazada.', 'info');
  }
}
