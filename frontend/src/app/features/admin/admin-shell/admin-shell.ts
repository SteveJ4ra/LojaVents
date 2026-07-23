import { Component } from '@angular/core';
import { AdminAttentionService } from '../../../core/services/admin-attention.service';
import { PortalLayout, PortalLink } from '../../../layout/portal-layout/portal-layout';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [PortalLayout],
  template: `<app-portal-layout title="Administración" [links]="links" />`
})
export class AdminShell {
  constructor(readonly attention: AdminAttentionService) {}

  readonly links: PortalLink[] = [
    { label: 'Resumen', route: '/admin', icon: '▦', attention: this.attention.pendingTotal },
    { label: 'Usuarios', route: '/admin/usuarios', icon: '♙' },
    { label: 'Locales', route: '/admin/locales', icon: '⌂', attention: this.attention.pendingVenues },
    { label: 'Verificaciones', route: '/admin/verificaciones', icon: '✓', attention: this.attention.pendingOwnerRequests }
  ];
}
