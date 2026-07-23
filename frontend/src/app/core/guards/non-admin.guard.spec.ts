import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { nonAdminGuard } from './non-admin.guard';

describe('nonAdminGuard', () => {
  let isAdmin: boolean;

  beforeEach(() => {
    isAdmin = false;
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { hasRole: () => isAdmin } }
      ]
    });
  });

  it('allows customer and owner accounts', () => {
    const result = TestBed.runInInjectionContext(() =>
      nonAdminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });

  it('redirects administrator accounts to the forbidden page', () => {
    isAdmin = true;
    const result = TestBed.runInInjectionContext(() =>
      nonAdminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    ) as UrlTree;

    expect(TestBed.inject(Router).serializeUrl(result)).toBe('/403?reason=admin');
  });
});
