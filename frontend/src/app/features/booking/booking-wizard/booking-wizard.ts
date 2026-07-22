import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { BookingService } from '../../../core/services/booking.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PaymentService } from '../../../core/services/payment.service';
import { VenueService } from '../../../core/services/venue.service';
import { Booking, PaymentSimulationMode } from '../../../shared/models/booking.model';

@Component({
  selector: 'app-booking-wizard',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, RouterLink],
  templateUrl: './booking-wizard.html',
  styleUrl: './booking-wizard.scss'
})
export class BookingWizard {
  readonly venueId = this.route.snapshot.paramMap.get('id') ?? '';
  readonly venue = computed(() => this.venues.getById(this.venueId));
  readonly step = signal(1);
  readonly processing = signal(false);
  readonly checkingAvailability = signal(false);
  readonly result = signal<Booking | null>(null);
  readonly rejectedResult = signal<Booking | null>(null);
  readonly error = signal<string | null>(null);
  readonly minDate = new Date().toISOString().slice(0, 10);
  private readonly draftId = signal<string | null>(null);

  readonly eventForm = this.fb.nonNullable.group({
    date: ['', Validators.required],
    startTime: ['16:00', Validators.required],
    durationHours: [3, [Validators.required, Validators.min(1), Validators.max(12)]],
    attendees: [20, [Validators.required, Validators.min(1)]]
  });

  readonly billingForm = this.fb.nonNullable.group({
    city: ['Loja', Validators.required],
    neighborhood: ['', Validators.required],
    street: ['', Validators.required]
  });

  readonly rulesForm = this.fb.nonNullable.group({
    acceptRules: [false, Validators.requiredTrue],
    acceptCancellation: [false, Validators.requiredTrue]
  });

  private readonly durationHours = toSignal(
    this.eventForm.controls.durationHours.valueChanges,
    { initialValue: this.eventForm.controls.durationHours.value }
  );

  readonly subtotal = computed(() => {
    const venue = this.venue();
    return venue ? venue.pricePerHour * this.durationHours() : 0;
  });
  readonly serviceFee = computed(() => this.subtotal() * 0.08);
  readonly total = computed(() => this.subtotal() + this.serviceFee());

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fb: FormBuilder,
    readonly venues: VenueService,
    readonly auth: AuthService,
    private readonly bookings: BookingService,
    private readonly payments: PaymentService,
    private readonly notifications: NotificationService
  ) {
    this.venues.loadPublicVenue(this.venueId).subscribe({ error: () => undefined });
  }

  nextFromEvent(): void {
    const venue = this.venue();
    if (!venue || this.eventForm.invalid) {
      this.eventForm.markAllAsTouched();
      return;
    }
    if (this.eventForm.controls.attendees.value > venue.capacity) {
      this.notifications.show(`La capacidad máxima es de ${venue.capacity} personas.`, 'error');
      return;
    }

    const { date, startTime, durationHours } = this.eventForm.getRawValue();
    this.checkingAvailability.set(true);
    this.bookings.checkAvailability(venue.id, date, startTime, durationHours).subscribe({
      next: result => {
        this.checkingAvailability.set(false);
        if (!result.available) {
          this.notifications.show(result.message, 'error');
          return;
        }
        this.step.set(2);
      },
      error: (error: HttpErrorResponse) => {
        this.checkingAvailability.set(false);
        this.notifications.show(this.readError(error), 'error');
      }
    });
  }

  nextFromBilling(): void {
    if (this.billingForm.invalid) {
      this.billingForm.markAllAsTouched();
      return;
    }
    this.step.set(3);
  }

  nextFromRules(): void {
    if (this.rulesForm.invalid) {
      this.rulesForm.markAllAsTouched();
      return;
    }
    this.step.set(4);
  }

  back(): void {
    this.step.update(value => Math.max(1, value - 1));
    this.error.set(null);
    this.rejectedResult.set(null);
  }

  async pay(mode: PaymentSimulationMode): Promise<void> {
    const venue = this.venue();
    const user = this.auth.currentUser();
    if (!venue || !user || this.processing()) return;

    this.processing.set(true);
    this.error.set(null);
    this.rejectedResult.set(null);

    try {
      let reservationId = this.draftId();
      if (!reservationId) {
        const event = this.eventForm.getRawValue();
        const billing = this.billingForm.getRawValue();
        const draft = await firstValueFrom(this.bookings.create({
          venueId: venue.id,
          ...event,
          billingAddress: billing,
          acceptedRules: this.rulesForm.controls.acceptRules.value,
          acceptedCancellation: this.rulesForm.controls.acceptCancellation.value
        }));
        reservationId = draft.id;
        this.draftId.set(reservationId);
      }

      const booking = await firstValueFrom(this.payments.process(reservationId, mode));
      if (booking.status === 'COMPLETADA') {
        this.result.set(booking);
        this.step.set(5);
      } else {
        this.rejectedResult.set(booking);
        this.error.set(booking.paymentMessage ?? 'No fue posible procesar el pago.');
        this.draftId.set(null);
      }
    } catch (error) {
      this.error.set(this.readError(error));
    } finally {
      this.processing.set(false);
    }
  }

  private readError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.detail ?? error.error?.message ?? 'No fue posible completar la solicitud.';
    }
    return 'No fue posible completar la solicitud.';
  }
}
