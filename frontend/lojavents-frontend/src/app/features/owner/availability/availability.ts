import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';

@Component({
  selector: 'app-availability',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './availability.html',
  styleUrl: './availability.scss'
})
export class Availability {
  readonly ownerVenues = computed(() => {
    const id = this.auth.currentUser()?.id;
    return id ? this.venues.getByOwner(id) : [];
  });
  readonly selectedId = signal(this.route.snapshot.queryParamMap.get('venueId') ?? this.ownerVenues()[0]?.id ?? '');
  readonly selected = computed(() => this.venues.getById(this.selectedId()));
  readonly form = this.fb.nonNullable.group({
    date: ['', Validators.required],
    startTime: ['08:00', Validators.required],
    endTime: ['18:00', Validators.required],
    reason: ['No disponible', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly auth: AuthService,
    private readonly venues: VenueService,
    private readonly notifications: NotificationService
  ) {}

  select(event: Event): void {
    this.selectedId.set((event.target as HTMLSelectElement).value);
  }

  add(): void {
    if (!this.selectedId() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (value.startTime >= value.endTime) {
      this.notifications.show('La hora final debe ser posterior a la inicial.', 'error');
      return;
    }
    this.venues.addBlock(this.selectedId(), {
      id: `block-${crypto.randomUUID()}`,
      ...value
    });
    this.form.reset({ date: '', startTime: '08:00', endTime: '18:00', reason: 'No disponible' });
    this.notifications.show('Bloqueo agregado al calendario.', 'success');
  }

  remove(blockId: string): void {
    this.venues.removeBlock(this.selectedId(), blockId);
    this.notifications.show('Bloqueo eliminado.', 'success');
  }
}
