import { FormControl, FormGroup } from '@angular/forms';
import { fieldsMatchValidator, integerValidator, timeRangeValidator, trimmedRequiredValidator } from './form.validators';

describe('shared form validators', () => {
  it('requires matching confirmation fields', () => {
    const form = new FormGroup({
      password: new FormControl('secreto'),
      confirmation: new FormControl('distinto')
    });
    expect(fieldsMatchValidator('password', 'confirmation')(form)).toEqual({ fieldsMismatch: true });
  });

  it('rejects decimals and blank-only required values', () => {
    expect(integerValidator()(new FormControl(2.5))).toEqual({ integer: true });
    expect(trimmedRequiredValidator()(new FormControl('   '))).toEqual({ required: true });
  });

  it('requires an end time after the start time', () => {
    const form = new FormGroup({
      start: new FormControl('18:00'),
      end: new FormControl('08:00')
    });
    expect(timeRangeValidator('start', 'end')(form)).toEqual({ invalidTimeRange: true });
  });
});
