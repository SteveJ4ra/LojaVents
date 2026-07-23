import { FormControl, FormGroup } from '@angular/forms';
import { identityDocumentValidator } from './owner-request';

describe('identityDocumentValidator', () => {
  const validate = (documentType: string, identification: string) => {
    const form = new FormGroup({
      documentType: new FormControl(documentType),
      identification: new FormControl(identification)
    });
    return identityDocumentValidator()(form);
  };

  it('validates each supported identity document format', () => {
    expect(validate('CEDULA', '1104680135')).toBeNull();
    expect(validate('PASAPORTE', 'AB123456')).toBeNull();
    expect(validate('LICENCIA_CONDUCIR', 'LJ12345')).toBeNull();
  });

  it('does not apply the Ecuadorian ID format to every document type', () => {
    expect(validate('CEDULA', 'AB123456')).toEqual({ invalidIdentityDocument: true });
    expect(validate('PASAPORTE', '123')).toEqual({ invalidIdentityDocument: true });
  });
});
