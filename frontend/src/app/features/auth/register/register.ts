import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../login/auth.scss'
})
export class Register {
  readonly loading = signal(false);
  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{9,10}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    accept: [false, Validators.requiredTrue]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly notifications: NotificationService
  ) {}

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    const { fullName, email, phone, password } = this.form.getRawValue();
    this.loading.set(true);
    this.auth.register({ fullName, email, phone, password }).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: () => {
        this.notifications.show('Cuenta creada correctamente.', 'success');
        void this.router.navigateByUrl('/');
      },
      error: (error: HttpErrorResponse) => {
        this.notifications.show(this.errorMessage(error), 'error');
      }
    });
  }

  private errorMessage(error: HttpErrorResponse): string {
    return error.error?.detail
      ?? error.error?.message
      ?? 'No fue posible crear la cuenta. Verifica que el backend esté encendido.';
  }
}
