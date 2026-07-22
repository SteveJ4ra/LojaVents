import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { finalize, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminDashboardData,
  OwnerDashboardData
} from '../../shared/models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly adminState = signal<AdminDashboardData | null>(null);
  private readonly ownerState = signal<OwnerDashboardData | null>(null);
  private readonly adminLoadingState = signal(false);
  private readonly ownerLoadingState = signal(false);

  readonly admin = this.adminState.asReadonly();
  readonly owner = this.ownerState.asReadonly();
  readonly adminLoading = this.adminLoadingState.asReadonly();
  readonly ownerLoading = this.ownerLoadingState.asReadonly();

  constructor(private readonly http: HttpClient) {}

  loadAdmin(): Observable<AdminDashboardData> {
    this.adminLoadingState.set(true);
    return this.http
      .get<AdminDashboardData>(`${environment.apiBaseUrl}/admin/dashboard`)
      .pipe(
        tap(data => this.adminState.set(data)),
        finalize(() => this.adminLoadingState.set(false))
      );
  }

  loadOwner(): Observable<OwnerDashboardData> {
    this.ownerLoadingState.set(true);
    return this.http
      .get<OwnerDashboardData>(`${environment.apiBaseUrl}/propietario/dashboard`)
      .pipe(
        tap(data => this.ownerState.set(data)),
        finalize(() => this.ownerLoadingState.set(false))
      );
  }
}
