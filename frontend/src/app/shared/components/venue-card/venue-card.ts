import { Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Venue } from '../../models/venue.model';

@Component({
  selector: 'app-venue-card',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './venue-card.html',
  styleUrl: './venue-card.scss'
})
export class VenueCard {
  readonly venue = input.required<Venue>();
  readonly favorite = input(false);
  readonly showFavorite = input(true);
  readonly favoriteChange = output<string>();
}
