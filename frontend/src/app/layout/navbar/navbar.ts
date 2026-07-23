import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AdminAttentionService } from '../../core/services/admin-attention.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class Navbar {
  readonly open = signal(false);

  constructor(
    readonly auth: AuthService,
    readonly adminAttention: AdminAttentionService
  ) {}

  close(): void {
    this.open.set(false);
  }
}
