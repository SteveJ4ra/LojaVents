import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `<section class="error-page"><span>404</span><h1>Página no encontrada</h1><p>La dirección que abriste no existe en LojaVents.</p><a class="btn btn-primary" routerLink="/">Volver al inicio</a></section>`,
  styles: [`.error-page{min-height:60vh;display:grid;place-items:center;align-content:center;text-align:center;padding:40px}.error-page span{font-size:5rem;font-weight:900;color:var(--primary-soft)}.error-page h1{margin:0}.error-page p{color:var(--text-muted);margin-bottom:24px}`]
})
export class NotFound {}
