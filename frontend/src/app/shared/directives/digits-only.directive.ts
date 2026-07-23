import { Directive, HostListener } from '@angular/core';

@Directive({
  selector: 'input[appDigitsOnly]',
  standalone: true
})
export class DigitsOnlyDirective {
  @HostListener('beforeinput', ['$event'])
  preventInvalidInput(event: InputEvent): void {
    if (event.data && /\D/.test(event.data)) {
      event.preventDefault();
    }
  }

  @HostListener('input', ['$event'])
  sanitizeInput(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    if (!input) return;
    const sanitized = input.value.replace(/\D/g, '');
    if (sanitized === input.value) return;

    input.value = sanitized;
    input.dispatchEvent(new Event('input', { bubbles: true }));
  }
}
