import { computed, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { RegisterRequest, User, UserRole } from '../../shared/models/user.model';
import { MockStoreService } from './mock-store.service';

export interface LoginResult {
  success: boolean;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sessionKey = 'lojavents-session-user-id';
  private readonly currentUserId = signal<string | null>(localStorage.getItem(this.sessionKey));

  readonly currentUser = computed<User | null>(() => {
    const id = this.currentUserId();
    return id ? this.store.users().find(user => user.id === id) ?? null : null;
  });

  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  constructor(
    private readonly store: MockStoreService,
    private readonly router: Router
  ) {}

  login(email: string, password: string): LoginResult {
    const normalized = email.trim().toLowerCase();
    const user = this.store.users().find(item => item.email.toLowerCase() === normalized);

    if (!user || this.store.credentials()[normalized] !== password) {
      return { success: false, message: 'Correo o contraseña incorrectos.' };
    }
    if (user.status !== 'ACTIVO') {
      return { success: false, message: 'La cuenta no se encuentra activa.' };
    }

    this.currentUserId.set(user.id);
    localStorage.setItem(this.sessionKey, user.id);
    return { success: true, message: 'Sesión iniciada correctamente.' };
  }

  register(request: RegisterRequest): LoginResult {
    const email = request.email.trim().toLowerCase();
    if (this.store.users().some(user => user.email.toLowerCase() === email)) {
      return { success: false, message: 'Ya existe una cuenta con ese correo.' };
    }

    const user: User = {
      id: `user-${crypto.randomUUID()}`,
      fullName: request.fullName.trim(),
      email,
      phone: request.phone.trim(),
      roles: ['CLIENTE'],
      status: 'ACTIVO',
      ownerVerificationStatus: 'NO_SOLICITADA',
      createdAt: new Date().toISOString()
    };

    this.store.users.update(users => [...users, user]);
    this.store.credentials.update(credentials => ({ ...credentials, [email]: request.password }));
    this.store.persist();
    this.currentUserId.set(user.id);
    localStorage.setItem(this.sessionKey, user.id);

    return { success: true, message: 'Cuenta creada correctamente.' };
  }

  logout(): void {
    this.currentUserId.set(null);
    localStorage.removeItem(this.sessionKey);
    void this.router.navigateByUrl('/');
  }

  hasRole(role: UserRole): boolean {
    return this.currentUser()?.roles.includes(role) ?? false;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  refreshSession(): void {
    this.currentUserId.set(localStorage.getItem(this.sessionKey));
  }
}
