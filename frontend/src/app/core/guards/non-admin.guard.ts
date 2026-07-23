import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const nonAdminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasRole('ADMINISTRADOR')
    ? router.createUrlTree(['/403'], { queryParams: { reason: 'admin' } })
    : true;
};
