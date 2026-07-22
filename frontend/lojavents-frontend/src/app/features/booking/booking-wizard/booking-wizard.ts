import { CurrencyPipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { BookingService } from '../../../core/services/booking.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PaymentService } from '../../../core/services/payment.service';
import { VenueService } from '../../../core/services/venue.service';
import { Booking } from '../../../shared/models/booking.model';
import { PaymentSimulationMode } from '../../../shared/models/payment.model';

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
  readonly result = signal<Booking | null>(null);
  readonly error = signal<string | null>(null);

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

  readonly subtotal = computed(() => {
    const venue = this.venue();
    return venue ? venue.pricePerHour * this.eventForm.controls.durationHours.value : 0;
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
  ) {}

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
    const blocked = venue.blockedSlots.some(block =>
      block.date === date && this.overlaps(startTime, durationHours, block.startTime, block.endTime)
    );
    if (blocked || !this.bookings.isSlotAvailable(venue.id, date, startTime, durationHours)) {
      this.notifications.show('El horario seleccionado no está disponible.', 'error');
      return;
    }
    this.step.set(2);
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
  }

  async pay(mode: PaymentSimulationMode): Promise<void> {
    const venue = this.venue();
    const user = this.auth.currentUser();
    if (!venue || !user || this.processing()) return;

    this.processing.set(true);
    this.error.set(null);
    const payment = await this.payments.process(mode);

    const event = this.eventForm.getRawValue();
    const billing = this.billingForm.getRawValue();

    if (!payment.success) {
      this.error.set(payment.message);
      this.processing.set(false);
      return;
    }

    const booking = this.bookings.create({
      userId: user.id,
      venueId: venue.id,
      ...event,
      billingAddress: billing,
      subtotal: this.subtotal(),
      serviceFee: this.serviceFee(),
      total: this.total(),
      status: 'COMPLETADA',
      paymentReference: payment.reference
    });

    this.result.set(booking);
    this.step.set(5);
    this.processing.set(false);
  }

  private overlaps(startTime: string, duration: number, blockStart: string, blockEnd: string): boolean {
    const start = this.toMinutes(startTime);
    const end = start + duration * 60;
    return start < this.toMinutes(blockEnd) && end > this.toMinutes(blockStart);
  }

  private toMinutes(value: string): number {
    const [h, m] = value.split(':').map(Number);
    return h * 60 + m;
  }
}
