import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { VenueService } from '../../../core/services/venue.service';
import { Venue, VenueSaveRequest } from '../../../shared/models/venue.model';

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
  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    shortDescription: ['', Validators.required],
    description: ['', Validators.required],
    neighborhood: ['', Validators.required],
    address: ['', Validators.required],
    pricePerHour: [40, [Validators.required, Validators.min(1)]],
    capacity: [30, [Validators.required, Validators.min(1)]],
    eventTypes: [<string[]>[], Validators.required],
    amenities: [<string[]>[], Validators.required],
    rules: ['', Validators.required],
    cancellationPolicy: ['', Validators.required],
    image: ['/images/venue-new.svg', Validators.required]
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
            cancellationPolicy: venue.cancellationPolicy,
            image: venue.images[0] ?? '/images/venue-new.svg'
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
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
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
      pricePerHour: value.pricePerHour,
      capacity: value.capacity,
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
        error: () => { this.saving.set(false); this.notifications.show('No fue posible cargar las imagenes.', 'error'); }
      });
    } else {
      persist(existing?.images ?? []);
    }
  }

  imagesChanged(event: Event): void {
    this.imageFiles.set(Array.from((event.target as HTMLInputElement).files ?? []));
  }

  cancel(): void {
    void this.router.navigateByUrl('/propietario/locales');
  }

}
