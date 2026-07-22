import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-owner-request',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StatusBadge],
  templateUrl: './owner-request.html',
  styleUrl: './owner-request.scss'
})
export class OwnerRequest {
  readonly user = this.auth.currentUser;
  readonly form = this.fb.nonNullable.group({
    identification: ['', [Validators.required, Validators.minLength(10)]],
    documentReference: ['', Validators.required],
    notes: ['', [Validators.required, Validators.minLength(15)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    readonly auth: AuthService,
    private readonly users: UserService,
    private readonly notifications: NotificationService
  ) {}

  fileChanged(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.form.controls.documentReference.setValue(input.files?.[0]?.name ?? '');
  }

  submit(): void {
    const user = this.user();
    if (!user || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.users.submitOwnerRequest({
      userId: user.id,
      ...this.form.getRawValue(),
      submittedAt: new Date().toISOString()
    });
    this.auth.refreshSession();
    this.notifications.show('Solicitud enviada para revisión.', 'success');
  }
}
