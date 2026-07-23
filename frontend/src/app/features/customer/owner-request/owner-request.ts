import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { IdentityDocumentType, OwnerVerificationRequest } from '../../../shared/models/user.model';

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
    documentType: ['CEDULA' as IdentityDocumentType, Validators.required],
    identification: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20)]],
    notes: ['', [Validators.required, Validators.minLength(15), Validators.maxLength(1200)]]
  }, { validators: identityDocumentValidator() });

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
    this.users.submitOwnerRequest(value.documentType, value.identification, value.notes, document).pipe(
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

export function identityDocumentValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const type = control.get('documentType')?.value as IdentityDocumentType | undefined;
    const number = String(control.get('identification')?.value ?? '').trim().toUpperCase();
    if (!type || !number) return null;

    const valid = type === 'CEDULA'
      ? /^[0-9]{10}$/.test(number)
      : type === 'PASAPORTE'
        ? /^[A-Z0-9]{6,20}$/.test(number)
        : /^[A-Z0-9]{5,20}$/.test(number);
    return valid ? null : { invalidIdentityDocument: true };
  };
}
