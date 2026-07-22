import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  UserRole
} from '../../shared/models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'lojavents-access-token';
  private readonly userKey = 'lojavents-session-user';
  private readonly apiUrl = `${environment.apiBaseUrl}/auth`;

  private readonly accessTokenState = signal<string | null>(
    localStorage.getItem(this.tokenKey)
  );
  private readonly currentUserState = signal<User | null>(this.loadStoredUser());

  readonly currentUser = this.currentUserState.asReadonly();
  readonly isAuthenticated = computed(
    () => Boolean(this.accessTokenState() && this.currentUserState())
  );

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {
    if (this.accessTokenState()) {
      queueMicrotask(() => this.refreshSession());
    }
  }

  login(email: string, password: string): Observable<AuthResponse> {
    const request: LoginRequest = {
      email: email.trim().toLowerCase(),
      password
    };

    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.saveSession(response))
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    const payload: RegisterRequest = {
      ...request,
      fullName: request.fullName.trim(),
      email: request.email.trim().toLowerCase(),
      phone: request.phone.trim()
    };

    return this.http.post<AuthResponse>(`${this.apiUrl}/registro`, payload).pipe(
      tap(response => this.saveSession(response))
    );
  }

  refreshSession(): void {
    if (!this.accessTokenState()) {
      this.clearSession();
      return;
    }

    this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {}).subscribe({
      next: response => this.saveSession(response),
      error: () => this.clearSession()
    });
  }

  logout(): void {
    this.clearSession();
    void this.router.navigateByUrl('/');
  }

  hasRole(role: UserRole): boolean {
    return this.currentUserState()?.roles.includes(role) ?? false;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  getAccessToken(): string | null {
    return this.accessTokenState();
  }

  updateCurrentUser(user: User): void {
    this.storeUser(user);
  }

  private saveSession(response: AuthResponse): void {
    this.accessTokenState.set(response.accessToken);
    localStorage.setItem(this.tokenKey, response.accessToken);
    this.storeUser(response.user);
    localStorage.removeItem('lojavents-session-user-id');
  }

  private storeUser(user: User): void {
    this.currentUserState.set(user);
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }

  private clearSession(): void {
    this.accessTokenState.set(null);
    this.currentUserState.set(null);
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem('lojavents-session-user-id');
  }

  private loadStoredUser(): User | null {
    try {
      const raw = localStorage.getItem(this.userKey);
      return raw ? JSON.parse(raw) as User : null;
    } catch {
      localStorage.removeItem(this.userKey);
      return null;
    }
  }
}
