import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { Venue, VenueSaveRequest } from '../../../shared/models/venue.model';
import { integerValidator, trimmedRequiredValidator } from '../../../shared/validators/form.validators';

@Component({
  selector: 'app-venue-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './venue-form.html',
  styleUrl: './venue-form.scss'
})
export class VenueForm {
  readonly eventTypeOptions = [
    'Bodas', 'Cumpleaños', 'Quinceaños', 'Graduaciones', 'Corporativo',
    'Eventos sociales', 'Conferencias', 'Talleres', 'Reuniones', 'Fotografía'
  ];
  readonly amenityOptions = [
    'Estacionamiento', 'Wi-Fi', 'Sonido', 'Proyector', 'Cocina',
    'Aire acondicionado', 'Área verde', 'Zona cubierta', 'Mobiliario', 'Iluminación'
  ];
  readonly id = this.route.snapshot.paramMap.get('id');
  readonly existing = signal<Venue | null>(null);
  readonly editing = Boolean(this.id);
  readonly saving = signal(false);
  readonly loading = signal(Boolean(this.id));
  readonly imageFiles = signal<File[]>([]);
  readonly imageTouched = signal(false);
  readonly imageError = signal<string | null>(null);
  readonly form = this.fb.nonNullable.group({
    name: ['', [trimmedRequiredValidator(), Validators.maxLength(160)]],
    shortDescription: ['', [trimmedRequiredValidator(), Validators.maxLength(240)]],
    description: ['', [trimmedRequiredValidator(), Validators.maxLength(5000)]],
    neighborhood: ['', [trimmedRequiredValidator(), Validators.maxLength(120)]],
    address: ['', [trimmedRequiredValidator(), Validators.maxLength(240)]],
    pricePerHour: [40, [Validators.required, Validators.min(1)]],
    capacity: [30, [Validators.required, Validators.min(1), Validators.max(10000), integerValidator()]],
    eventTypes: [<string[]>[], Validators.required],
    amenities: [<string[]>[], Validators.required],
    rules: ['', [trimmedRequiredValidator(), Validators.maxLength(15000)]],
    cancellationPolicy: ['', [trimmedRequiredValidator(), Validators.maxLength(3000)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    readonly router: Router,
    private readonly venues: VenueService,
    private readonly notifications: NotificationService
  ) {
    if (this.id) {
      this.venues.loadOwnerVenue(this.id).subscribe({
        next: venue => {
          this.existing.set(venue);
          this.form.patchValue({
            name: venue.name,
            shortDescription: venue.shortDescription,
            description: venue.description,
            neighborhood: venue.neighborhood,
            address: venue.address,
            pricePerHour: venue.pricePerHour,
            capacity: venue.capacity,
            eventTypes: venue.eventTypes,
            amenities: venue.amenities,
            rules: venue.rules.join('\n'),
            cancellationPolicy: venue.cancellationPolicy
          });
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.notifications.show('No fue posible cargar el local.', 'error');
          void this.router.navigateByUrl('/propietario/locales');
        }
      });
    }
  }

  save(): void {
    const hasImages = this.imageFiles().length > 0 || (this.existing()?.images.length ?? 0) > 0;
    if (this.form.invalid || !hasImages || this.imageError() || this.saving()) {
      this.form.markAllAsTouched();
      this.imageTouched.set(true);
      return;
    }

    const value = this.form.getRawValue();
    const existing = this.existing();
    const persist = (images: string[]) => {
      const request: VenueSaveRequest = {
      name: value.name.trim(),
      shortDescription: value.shortDescription.trim(),
      description: value.description.trim(),
      neighborhood: value.neighborhood.trim(),
      address: value.address.trim(),
      pricePerHour: Number(value.pricePerHour),
      capacity: Number(value.capacity),
      eventTypes: value.eventTypes,
      amenities: value.amenities,
      rules: value.rules.split('\n').map(item => item.trim()).filter(Boolean),
      cancellationPolicy: value.cancellationPolicy.trim(),
        images
      };
      const operation = this.id ? this.venues.update(this.id, request) : this.venues.create(request);

      operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.notifications.show('El local fue enviado a revisión administrativa.', 'success');
        void this.router.navigateByUrl('/propietario/locales');
      },
      error: error => {
        this.saving.set(false);
        this.notifications.show(error?.error?.detail ?? 'No fue posible guardar el local.', 'error');
      }
      });
    };

    this.saving.set(true);
    const files = this.imageFiles();
    if (files.length) {
      this.venues.uploadImages(files).subscribe({
        next: persist,
        error: () => { this.saving.set(false); this.notifications.show('No fue posible cargar las imágenes.', 'error'); }
      });
    } else {
      persist(existing?.images ?? []);
    }
  }

  imagesChanged(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    this.imageTouched.set(true);
    this.imageError.set(null);
    if (files.length > 10) {
      this.imageFiles.set([]);
      this.imageError.set('Puedes cargar como máximo 10 fotografías.');
      input.value = '';
      return;
    }
    const invalid = files.some(file =>
      !['image/png', 'image/jpeg', 'image/webp'].includes(file.type) || file.size > 8 * 1024 * 1024
    );
    if (invalid) {
      this.imageFiles.set([]);
      this.imageError.set('Cada fotografía debe ser PNG, JPG o WEBP y pesar hasta 8 MB.');
      input.value = '';
      return;
    }
    this.imageFiles.set(files);
  }

  toggleSelection(field: 'eventTypes' | 'amenities', option: string, checked: boolean): void {
    const control = this.form.controls[field];
    const next = checked
      ? [...control.value, option]
      : control.value.filter(value => value !== option);
    control.setValue(next);
    control.markAsTouched();
  }

  cancel(): void {
    void this.router.navigateByUrl('/propietario/locales');
  }

}
