import { computed, effect, Injectable, signal } from '@angular/core';
import { AuthService } from './auth.service';
import { UserService } from './user.service';
import { VenueService } from './venue.service';

@Injectable({ providedIn: 'root' })
export class AdminAttentionService {
  private readonly loadedForUser = signal<string | null>(null);

  readonly pendingOwnerRequests = computed(() =>
    this.users.ownerRequests().filter(request => request.status === 'PENDIENTE').length
  );
  readonly pendingVenues = computed(() =>
    this.venues.venues().filter(venue => venue.pendingReview).length
  );
  readonly hasPending = computed(() =>
    this.pendingOwnerRequests() > 0 || this.pendingVenues() > 0
  );
  readonly pendingTotal = computed(() =>
    this.pendingOwnerRequests() + this.pendingVenues()
  );

  constructor(
    private readonly auth: AuthService,
    private readonly users: UserService,
    private readonly venues: VenueService
  ) {
    effect(() => {
      const user = this.auth.currentUser();
      if (!user?.roles.includes('ADMINISTRADOR')) {
        this.loadedForUser.set(null);
        return;
      }
      if (this.loadedForUser() === user.id) return;
      this.loadedForUser.set(user.id);
      this.refresh();
    });
  }

  refresh(): void {
    this.users.loadOwnerRequests().subscribe({ error: () => undefined });
    this.venues.loadAdminVenues().subscribe({ error: () => undefined });
  }
}
