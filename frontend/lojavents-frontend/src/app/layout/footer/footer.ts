import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer>
      <div class="container footer-grid">
        <div>
          <strong>LojaVents</strong>
          <p>Encuentra y reserva espacios para eventos en Loja.</p>
        </div>
        <div>
          <a routerLink="/locales">Explorar locales</a>
          <a routerLink="/registro">Crear cuenta</a>
        </div>
        <p class="copy">Proyecto académico · {{ year }}</p>
      </div>
    </footer>
  `,
  styles: [`
    footer { margin-top:auto; background:#17211c; color:white; padding:38px 0; }
    .footer-grid { display:grid; grid-template-columns:2fr 1fr auto; gap:30px; align-items:start; }
    strong { font-size:1.2rem; }
    p { color:#cbd5cf; margin:.5rem 0 0; }
    a { display:block; color:#e5eee8; text-decoration:none; margin-bottom:.55rem; }
    .copy { margin:0; }
    @media(max-width:700px){ .footer-grid{grid-template-columns:1fr;} }
  `]
})
export class Footer {
  readonly year = new Date().getFullYear();
}
