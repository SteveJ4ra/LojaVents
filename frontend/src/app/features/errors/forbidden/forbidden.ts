import { Component } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [RouterLink],
  template: `<section class="error-page"><span>403</span>@if (ownerRequest) { <h1>Esta seccion es para propietarios</h1><p>Tu cuenta puede seguir explorando y reservando locales. Solicita el rol de propietario para publicar y gestionar tus propios espacios.</p><a class="btn btn-primary" routerLink="/convertirme-en-propietario">Solicitar rol de propietario</a> } @else { <h1>No tienes permiso para entrar aqui</h1><p>La pantalla solicitada requiere otro rol de usuario.</p><a class="btn btn-primary" routerLink="/">Volver al inicio</a> }</section>`,
  styles: [`.error-page{min-height:60vh;display:grid;place-items:center;align-content:center;text-align:center;padding:40px}.error-page span{font-size:5rem;font-weight:900;color:var(--primary-soft)}.error-page h1{margin:0}.error-page p{color:var(--text-muted);margin-bottom:24px;max-width:560px}`]
})
export class Forbidden {
  readonly ownerRequest = this.route.snapshot.queryParamMap.get('reason') === 'owner';

  constructor(private readonly route: ActivatedRoute) {}
}
