import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span class="badge" [class]="cssClass()">{{ label() }}</span>`,
  styles: [`
    .badge { display:inline-flex; align-items:center; padding:.3rem .65rem; border-radius:999px; font-size:.78rem; font-weight:750; }
    .success { background:#dcfce7; color:#166534; }
    .warning { background:#fef3c7; color:#92400e; }
    .danger { background:#fee2e2; color:#991b1b; }
    .neutral { background:#e5e7eb; color:#374151; }
    .info { background:#dbeafe; color:#1e40af; }
  `]
})
export class StatusBadge {
  readonly status = input.required<string>();

  readonly label = computed(() => {
    const labels: Record<string, string> = {
      EN_PROCESO: 'En proceso',
      CONFIRMADA: 'Confirmada',
      RECHAZADA: 'Rechazada',
      CANCELADA: 'Cancelada',
      ACTIVO: 'Activo',
      INACTIVO: 'Inactivo',
      SUSPENDIDO: 'Suspendido',
      PENDIENTE: 'Pendiente',
      APROBADA: 'Aprobada',
      NO_SOLICITADA: 'No solicitada',
      CLIENTE: 'Cliente',
      PROPIETARIO: 'Propietario',
      ADMINISTRADOR: 'Administrador'
    };
    return labels[this.status()] ?? this.status().replaceAll('_', ' ');
  });
  readonly cssClass = computed(() => {
    const status = this.status();
    if (['CONFIRMADA', 'ACTIVO', 'APROBADA'].includes(status)) return 'success';
    if (['PENDIENTE', 'EN_PROCESO'].includes(status)) return 'warning';
    if (['RECHAZADA', 'SUSPENDIDO', 'CANCELADA'].includes(status)) return 'danger';
    if (['INACTIVO', 'NO_SOLICITADA'].includes(status)) return 'neutral';
    return 'info';
  });
}
