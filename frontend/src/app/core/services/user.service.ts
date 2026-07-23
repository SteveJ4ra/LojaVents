import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  IdentityDocumentType,
  OwnerRequestReviewRequest,
  OwnerVerificationRequest,
  PasswordChangeRequest,
  ProfileUpdateRequest,
  User,
  UserStatus
} from '../../shared/models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly profileUrl = `${environment.apiBaseUrl}/perfil`;
  private readonly ownerRequestUrl = `${environment.apiBaseUrl}/solicitud-propietario`;
  private readonly adminUsersUrl = `${environment.apiBaseUrl}/admin/usuarios`;
  private readonly adminOwnerRequestsUrl = `${environment.apiBaseUrl}/admin/solicitudes-propietario`;

  private readonly usersState = signal<User[]>([]);
  private readonly ownerRequestsState = signal<OwnerVerificationRequest[]>([]);

  constructor(private readonly http: HttpClient) {}

  all(): User[] {
    return this.usersState();
  }

  ownerRequests(): OwnerVerificationRequest[] {
    return this.ownerRequestsState();
  }

  updateProfile(changes: ProfileUpdateRequest): Observable<User> {
    return this.http.put<User>(this.profileUrl, changes);
  }

  changePassword(request: PasswordChangeRequest): Observable<void> {
    return this.http.put<void>(`${this.profileUrl}/password`, request);
  }

  deactivateOwnAccount(): Observable<void> {
    return this.http.delete<void>(this.profileUrl);
  }

  loadUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.adminUsersUrl).pipe(
      tap(users => this.usersState.set(users))
    );
  }

  setStatus(userId: string, status: UserStatus): Observable<User> {
    return this.http.patch<User>(`${this.adminUsersUrl}/${userId}/estado`, { status }).pipe(
      tap(updated => this.usersState.update(users =>
        users.map(user => user.id === updated.id ? updated : user)
      ))
    );
  }

  loadMyOwnerRequest(): Observable<OwnerVerificationRequest | null> {
    return this.http.get<OwnerVerificationRequest | null>(`${this.ownerRequestUrl}/me`);
  }

  submitOwnerRequest(documentType: IdentityDocumentType, identification: string, notes: string, document: File): Observable<OwnerVerificationRequest> {
    const payload = new FormData();
    payload.set('documentType', documentType);
    payload.set('identification', identification);
    payload.set('notes', notes);
    payload.set('document', document);
    return this.http.post<OwnerVerificationRequest>(this.ownerRequestUrl, payload);
  }

  loadOwnerRequests(status = 'PENDIENTE'): Observable<OwnerVerificationRequest[]> {
    return this.http.get<OwnerVerificationRequest[]>(
      this.adminOwnerRequestsUrl,
      { params: { status } }
    ).pipe(
      tap(requests => this.ownerRequestsState.set(requests))
    );
  }

  reviewOwnerRequest(
    requestId: string,
    request: OwnerRequestReviewRequest
  ): Observable<OwnerVerificationRequest> {
    return this.http.patch<OwnerVerificationRequest>(
      `${this.adminOwnerRequestsUrl}/${requestId}`,
      request
    ).pipe(
      tap(updated => this.ownerRequestsState.update(items =>
        items.filter(item => item.id !== updated.id)
      ))
    );
  }

  openOwnerRequestDocument(requestId: string): Observable<Blob> {
    return this.http.get(`${this.adminOwnerRequestsUrl}/${requestId}/documento`, { responseType: 'blob' });
  }
}
