import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { OwnerVerificationRequest } from '../../../shared/models/user.model';

@Component({
  selector: 'app-verifications',
  standalone: true,
  imports: [EmptyState],
  templateUrl: './verifications.html',
  styleUrl: './verifications.scss'
})
export class Verifications implements OnInit {
  readonly requests = computed(() => this.users.ownerRequests());
  readonly loading = signal(true);
  readonly reviewingId = signal<string | null>(null);

  constructor(
    private readonly users: UserService,
    private readonly notifications: NotificationService
  ) {}

  ngOnInit(): void {
    this.users.loadOwnerRequests().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  approve(request: OwnerVerificationRequest): void {
    const comment = prompt('Comentario opcional para la aprobación:', '') ?? '';
    this.reviewingId.set(request.id);
    this.users.reviewOwnerRequest(request.id, {
      decision: 'APROBAR',
      comment: comment.trim()
    }).pipe(
      finalize(() => this.reviewingId.set(null))
    ).subscribe({
      next: () => this.notifications.show('Rol de propietario aprobado.', 'success'),
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  reject(request: OwnerVerificationRequest): void {
    const comment = prompt('Indica el motivo del rechazo:');
    if (comment === null) return;
    if (comment.trim().length < 5) {
      this.notifications.show('Escribe un motivo de al menos 5 caracteres.', 'error');
      return;
    }

    this.reviewingId.set(request.id);
    this.users.reviewOwnerRequest(request.id, {
      decision: 'RECHAZAR',
      comment: comment.trim()
    }).pipe(
      finalize(() => this.reviewingId.set(null))
    ).subscribe({
      next: () => this.notifications.show('Solicitud rechazada.', 'info'),
      error: error => this.notifications.show(this.readError(error), 'error')
    });
  }

  openDocument(request: OwnerVerificationRequest): void {
    const preview = window.open('', '_blank');
    this.users.openOwnerRequestDocument(request.id).subscribe({
      next: document => {
        const url = URL.createObjectURL(document);
        if (preview) preview.location.href = url;
        else window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: error => {
        preview?.close();
        this.notifications.show(this.readError(error), 'error');
      }
    });
  }

  private readError(error: unknown): string {
    const httpError = error as HttpErrorResponse;
    return httpError.error?.detail ?? 'No fue posible revisar la solicitud.';
  }
}
