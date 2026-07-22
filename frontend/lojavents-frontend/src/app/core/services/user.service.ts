import { Injectable } from '@angular/core';
import { OwnerVerificationRequest, User, UserStatus } from '../../shared/models/user.model';
import { MockStoreService } from './mock-store.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly store: MockStoreService) {}

  all(): User[] {
    return this.store.users();
  }

  updateProfile(userId: string, changes: Pick<User, 'fullName' | 'phone'>): void {
    this.updateUser(userId, user => ({ ...user, ...changes }));
  }

  setStatus(userId: string, status: UserStatus): void {
    this.updateUser(userId, user => ({ ...user, status }));
  }

  submitOwnerRequest(request: OwnerVerificationRequest): void {
    this.store.ownerRequests.update(items => [
      ...items.filter(item => item.userId !== request.userId),
      request
    ]);
    this.updateUser(request.userId, user => ({ ...user, ownerVerificationStatus: 'PENDIENTE' }));
  }

  ownerRequests(): OwnerVerificationRequest[] {
    return this.store.ownerRequests();
  }

  approveOwner(userId: string): void {
    this.updateUser(userId, user => ({
      ...user,
      roles: user.roles.includes('PROPIETARIO') ? user.roles : [...user.roles, 'PROPIETARIO'],
      ownerVerificationStatus: 'APROBADA'
    }));
    this.store.ownerRequests.update(items => items.filter(item => item.userId !== userId));
    this.store.persist();
  }

  rejectOwner(userId: string): void {
    this.updateUser(userId, user => ({ ...user, ownerVerificationStatus: 'RECHAZADA' }));
    this.store.ownerRequests.update(items => items.filter(item => item.userId !== userId));
    this.store.persist();
  }

  private updateUser(userId: string, updater: (user: User) => User): void {
    this.store.users.update(users => users.map(user => user.id === userId ? updater(user) : user));
    this.store.persist();
  }
}
