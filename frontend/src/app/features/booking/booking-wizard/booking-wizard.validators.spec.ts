import { FormControl } from '@angular/forms';
import { integerValidator } from './booking-wizard';

describe('integerValidator', () => {
  const validator = integerValidator();

  it('accepts the reservation limits', () => {
    expect(validator(new FormControl(1))).toBeNull();
    expect(validator(new FormControl(12))).toBeNull();
  });

  it('rejects decimal durations', () => {
    expect(validator(new FormControl(1.5))).toEqual({ integer: true });
  });
});
