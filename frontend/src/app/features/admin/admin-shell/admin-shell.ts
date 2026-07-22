import { Component } from '@angular/core';
import { PortalLayout, PortalLink } from '../../../layout/portal-layout/portal-layout';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [PortalLayout],
  template: `<app-portal-layout title="Administración" [links]="links" />`
})
export class AdminShell {
  readonly links: PortalLink[] = [
    { label: 'Resumen', route: '/admin', icon: '▦' },
    { label: 'Usuarios', route: '/admin/usuarios', icon: '♙' },
    { label: 'Locales', route: '/admin/locales', icon: '⌂' },
    { label: 'Verificaciones', route: '/admin/verificaciones', icon: '✓' }
  ];
}
