import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { OwnerVerificationRequest } from '../../../shared/models/user.model';

@Component({
  selector: 'app-owner-request',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StatusBadge],
  templateUrl: './owner-request.html',
  styleUrl: './owner-request.scss'
})
export class OwnerRequest implements OnInit {
  readonly user = this.auth.currentUser;
  readonly request = signal<OwnerVerificationRequest | null>(null);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly document = signal<File | null>(null);

  readonly form = this.fb.nonNullable.group({
    identification: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(30)]],
    notes: ['', [Validators.required, Validators.minLength(15), Validators.maxLength(1200)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    readonly auth: AuthService,
    private readonly users: UserService,
    private readonly notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.auth.refreshSession();
    this.users.loadMyOwnerRequest().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: request => this.request.set(request),
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  fileChanged(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.document.set(input.files?.[0] ?? null);
  }

  submit(): void {
    const document = this.document();
    if (this.form.invalid || !document || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const value = this.form.getRawValue();
    this.users.submitOwnerRequest(value.identification, value.notes, document).pipe(
      finalize(() => this.submitting.set(false))
    ).subscribe({
      next: request => {
        this.request.set(request);
        this.auth.refreshSession();
        this.notifications.show('Solicitud enviada para revisión.', 'success');
      },
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  private readError(error: unknown): string {
    const httpError = error as HttpErrorResponse;
    return httpError.error?.detail ?? 'No fue posible procesar la solicitud.';
  }
}
