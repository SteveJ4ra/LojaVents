import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MockStoreService } from '../../../core/services/mock-store.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, StatusBadge],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile {
  readonly user = this.auth.currentUser;
  readonly form = this.fb.nonNullable.group({
    fullName: [this.user()?.fullName ?? '', [Validators.required, Validators.minLength(3)]],
    phone: [this.user()?.phone ?? '', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    readonly auth: AuthService,
    private readonly users: UserService,
    private readonly notifications: NotificationService,
    private readonly store: MockStoreService
  ) {}

  save(): void {
    const user = this.user();
    if (!user || this.form.invalid) return;
    this.users.updateProfile(user.id, this.form.getRawValue());
    this.auth.refreshSession();
    this.notifications.show('Perfil actualizado.', 'success');
  }

  deactivate(): void {
    const user = this.user();
    if (!user || !confirm('¿Seguro que deseas dar de baja la cuenta?')) return;
    this.users.setStatus(user.id, 'INACTIVO');
    this.auth.logout();
  }

  resetDemo(): void {
    if (!confirm('Esto eliminará los cambios locales de la demostración.')) return;
    this.auth.logout();
    this.store.reset();
  }
}
