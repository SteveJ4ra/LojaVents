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
import { ecuadorToday } from '../../../shared/utils/date-time';
import { integerValidator as buildIntegerValidator, trimmedRequiredValidator } from '../../../shared/validators/form.validators';

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
  readonly availabilityError = signal<string | null>(null);
  readonly availabilityErrorField = signal<'date' | 'time'>('time');
  readonly minDate = ecuadorToday();
  private readonly draftId = signal<string | null>(null);

  readonly eventForm = this.fb.nonNullable.group({
    date: ['', Validators.required],
    startTime: ['16:00', Validators.required],
    durationHours: [3, [Validators.required, Validators.min(1), Validators.max(12), integerValidator()]],
    attendees: [20, [Validators.required, Validators.min(1), integerValidator()]]
  });

  readonly billingForm = this.fb.nonNullable.group({
    city: ['Loja', [trimmedRequiredValidator(), Validators.maxLength(120)]],
    neighborhood: ['', [trimmedRequiredValidator(), Validators.maxLength(120)]],
    street: ['', [trimmedRequiredValidator(), Validators.maxLength(300)]]
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
    this.venues.loadPublicVenue(this.venueId).subscribe({
      next: venue => {
        this.eventForm.controls.attendees.addValidators(Validators.max(venue.capacity));
        this.eventForm.controls.attendees.updateValueAndValidity();
      },
      error: () => undefined
    });
    this.eventForm.valueChanges.subscribe(() => this.availabilityError.set(null));
  }

  nextFromEvent(): void {
    const venue = this.venue();
    if (!venue || this.eventForm.invalid) {
      this.eventForm.markAllAsTouched();
      return;
    }
    const { date, startTime, durationHours } = this.eventForm.getRawValue();
    this.availabilityError.set(null);
    this.checkingAvailability.set(true);
    this.bookings.checkAvailability(venue.id, date, startTime, durationHours).subscribe({
      next: result => {
        this.checkingAvailability.set(false);
        if (!result.available) {
          this.setAvailabilityError(result.message);
          return;
        }
        this.step.set(2);
      },
      error: (error: HttpErrorResponse) => {
        this.checkingAvailability.set(false);
        this.setAvailabilityError(this.readError(error));
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
      if (booking.status === 'CONFIRMADA') {
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

  private setAvailabilityError(message: string): void {
    this.availabilityErrorField.set(/fecha|pasad|local no está disponible/i.test(message) ? 'date' : 'time');
    this.availabilityError.set(message);
  }
}

export const integerValidator = buildIntegerValidator;
