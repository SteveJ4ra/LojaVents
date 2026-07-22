import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="empty-state">
      <div class="icon">{{ icon() }}</div>
      <h2>{{ title() }}</h2>
      <p>{{ message() }}</p>
      @if (link() && linkLabel()) {
        <a class="btn btn-primary" [routerLink]="link()">{{ linkLabel() }}</a>
      }
    </section>
  `,
  styles: [`
    .empty-state { text-align:center; padding:4rem 1rem; border:1px dashed var(--border); border-radius:20px; background:var(--surface); }
    .icon { font-size:3rem; }
    h2 { margin:.8rem 0 .4rem; }
    p { color:var(--text-muted); max-width:520px; margin:0 auto 1.4rem; }
  `]
})
export class EmptyState {
  readonly icon = input('⌂');
  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly link = input<string | readonly any[] | null>(null);
  readonly linkLabel = input<string | null>(null);
}
