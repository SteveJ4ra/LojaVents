import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Eye, EyeOff, LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { DigitsOnlyDirective } from '../../../shared/directives/digits-only.directive';
import { fieldsMatchValidator } from '../../../shared/validators/form.validators';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, StatusBadge, LucideAngularModule, DigitsOnlyDirective],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile {
  readonly user = this.auth.currentUser;
  readonly savingProfile = signal(false);
  readonly savingPassword = signal(false);
  readonly showCurrentPassword = signal(false);
  readonly showNewPassword = signal(false);
  readonly showConfirmation = signal(false);
  readonly EyeIcon = Eye;
  readonly EyeOffIcon = EyeOff;

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{9,10}$/)]]
  }, { validators: fieldsMatchValidator('newPassword', 'confirmPassword') });

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]],
    confirmPassword: ['', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    readonly auth: AuthService,
    private readonly users: UserService,
    private readonly notifications: NotificationService
  ) {
    effect(() => {
      const user = this.user();
      if (user) {
        this.form.patchValue({
          fullName: user.fullName,
          phone: user.phone
        }, { emitEvent: false });
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.savingProfile()) {
      this.form.markAllAsTouched();
      return;
    }

    this.savingProfile.set(true);
    this.users.updateProfile(this.form.getRawValue()).pipe(
      finalize(() => this.savingProfile.set(false))
    ).subscribe({
      next: user => {
        this.auth.updateCurrentUser(user);
        this.notifications.show('Perfil actualizado.', 'success');
      },
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  changePassword(): void {
    const value = this.passwordForm.getRawValue();
    if (this.passwordForm.invalid || this.savingPassword()) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.savingPassword.set(true);
    this.users.changePassword({
      currentPassword: value.currentPassword,
      newPassword: value.newPassword
    }).pipe(
      finalize(() => this.savingPassword.set(false))
    ).subscribe({
      next: () => {
        this.passwordForm.reset();
        this.notifications.show('Contraseña actualizada.', 'success');
      },
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  deactivate(): void {
    const user = this.user();
    if (!user || !confirm('¿Seguro que deseas dar de baja tu cuenta? No podrás iniciar sesión hasta que un administrador la reactive.')) {
      return;
    }

    this.users.deactivateOwnAccount().subscribe({
      next: () => {
        this.notifications.show('La cuenta fue dada de baja.', 'info');
        this.auth.logout();
      },
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  private readError(error: unknown): string {
    const httpError = error as HttpErrorResponse;
    return httpError.error?.detail ?? 'No fue posible completar la operación.';
  }
}
