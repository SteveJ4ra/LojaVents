import { Component } from '@angular/core';
import { PortalLayout, PortalLink } from '../../../layout/portal-layout/portal-layout';

@Component({
  selector: 'app-owner-shell',
  standalone: true,
  imports: [PortalLayout],
  template: `<app-portal-layout title="Panel de propietario" [links]="links" />`
})
export class OwnerShell {
  readonly links: PortalLink[] = [
    { label: 'Resumen', route: '/propietario', icon: '▦' },
    { label: 'Mis locales', route: '/propietario/locales', icon: '⌂' },
    { label: 'Registrar local', route: '/propietario/locales/nuevo', icon: '+' },
    { label: 'Disponibilidad', route: '/propietario/disponibilidad', icon: '◫' }
  ];
}
