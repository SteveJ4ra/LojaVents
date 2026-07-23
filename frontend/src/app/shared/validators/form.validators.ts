import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function fieldsMatchValidator(source: string, confirmation: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const sourceValue = control.get(source)?.value;
    const confirmationValue = control.get(confirmation)?.value;
    if (!sourceValue || !confirmationValue) return null;
    return sourceValue === confirmationValue ? null : { fieldsMismatch: true };
  };
}

export function integerValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value === null || control.value === '') return null;
    return Number.isInteger(Number(control.value)) ? null : { integer: true };
  };
}

export function trimmedRequiredValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null =>
    String(control.value ?? '').trim() ? null : { required: true };
}

export function timeRangeValidator(startField: string, endField: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const start = control.get(startField)?.value;
    const end = control.get(endField)?.value;
    if (!start || !end) return null;
    return start < end ? null : { invalidTimeRange: true };
  };
}
