import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { ecuadorToday } from '../../../shared/utils/date-time';
import { timeRangeValidator, trimmedRequiredValidator } from '../../../shared/validators/form.validators';

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
  readonly selectedId = signal(this.route.snapshot.queryParamMap.get('venueId') ?? '');
  readonly selected = computed(() => this.venues.getById(this.selectedId()));
  readonly saving = signal(false);
  readonly minDate = ecuadorToday();
  readonly form = this.fb.nonNullable.group({
    date: ['', Validators.required],
    startTime: ['08:00', Validators.required],
    endTime: ['18:00', Validators.required],
    reason: ['No disponible', [trimmedRequiredValidator(), Validators.maxLength(180)]]
  }, { validators: timeRangeValidator('startTime', 'endTime') });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly auth: AuthService,
    private readonly venues: VenueService,
    private readonly notifications: NotificationService
  ) {
    this.venues.loadOwnerVenues().subscribe({
      next: items => {
        if (!this.selectedId() && items.length) {
          this.selectedId.set(items[0].id);
        }
      },
      error: () => this.notifications.show('No fue posible cargar la disponibilidad.', 'error')
    });
  }

  select(event: Event): void {
    this.selectedId.set((event.target as HTMLSelectElement).value);
  }

  add(): void {
    if (!this.selectedId() || this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();

    this.saving.set(true);
    this.venues.addBlock(this.selectedId(), value).subscribe({
      next: () => {
        this.saving.set(false);
        this.form.reset({ date: '', startTime: '08:00', endTime: '18:00', reason: 'No disponible' });
        this.notifications.show('Bloqueo agregado al calendario.', 'success');
      },
      error: error => {
        this.saving.set(false);
        this.notifications.show(error?.error?.detail ?? 'No fue posible agregar el bloqueo.', 'error');
      }
    });
  }

  remove(blockId: string): void {
    this.venues.removeBlock(this.selectedId(), blockId).subscribe({
      next: () => this.notifications.show('Bloqueo eliminado.', 'success'),
      error: error => this.notifications.show(error?.error?.detail ?? 'No fue posible eliminar el bloqueo.', 'error')
    });
  }
}
