import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { Venue } from '../../../shared/models/venue.model';

@Component({
  selector: 'app-venue-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './venue-form.html',
  styleUrl: './venue-form.scss'
})
export class VenueForm {
  readonly id = this.route.snapshot.paramMap.get('id');
  readonly existing = this.id ? this.venues.getById(this.id) : undefined;
  readonly editing = Boolean(this.existing);
  readonly form = this.fb.nonNullable.group({
    name: [this.existing?.name ?? '', Validators.required],
    shortDescription: [this.existing?.shortDescription ?? '', Validators.required],
    description: [this.existing?.description ?? '', Validators.required],
    neighborhood: [this.existing?.neighborhood ?? '', Validators.required],
    address: [this.existing?.address ?? '', Validators.required],
    pricePerHour: [this.existing?.pricePerHour ?? 40, [Validators.required, Validators.min(1)]],
    capacity: [this.existing?.capacity ?? 30, [Validators.required, Validators.min(1)]],
    eventTypes: [this.existing?.eventTypes.join(', ') ?? '', Validators.required],
    amenities: [this.existing?.amenities.join(', ') ?? '', Validators.required],
    rules: [this.existing?.rules.join('\n') ?? '', Validators.required],
    cancellationPolicy: [this.existing?.cancellationPolicy ?? '', Validators.required],
    image: [this.existing?.images[0] ?? '/images/venue-new.svg']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    readonly router: Router,
    private readonly auth: AuthService,
    private readonly venues: VenueService,
    private readonly notifications: NotificationService
  ) {}

  save(): void {
    const user = this.auth.currentUser();
    if (!user || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const venue: Venue = {
      id: this.existing?.id ?? `venue-${crypto.randomUUID()}`,
      ownerId: this.existing?.ownerId ?? user.id,
      name: value.name,
      shortDescription: value.shortDescription,
      description: value.description,
      neighborhood: value.neighborhood,
      address: value.address,
      pricePerHour: value.pricePerHour,
      capacity: value.capacity,
      rating: this.existing?.rating ?? 0,
      reviewCount: this.existing?.reviewCount ?? 0,
      eventTypes: this.splitComma(value.eventTypes),
      amenities: this.splitComma(value.amenities),
      rules: value.rules.split('\n').map(item => item.trim()).filter(Boolean),
      cancellationPolicy: value.cancellationPolicy,
      images: this.existing?.images ?? [value.image],
      featured: this.existing?.featured ?? false,
      active: this.existing?.active ?? true,
      blockedSlots: this.existing?.blockedSlots ?? []
    };
    this.venues.save(venue);
    this.notifications.show(this.editing ? 'Local actualizado.' : 'Local registrado.', 'success');
    void this.router.navigateByUrl('/propietario/locales');
  }

  cancel(): void {
    void this.router.navigateByUrl('/propietario/locales');
  }

  private splitComma(value: string): string[] {
    return value.split(',').map(item => item.trim()).filter(Boolean);
  }
}
