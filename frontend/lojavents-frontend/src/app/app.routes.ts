import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/public/home/home').then(m => m.Home),
    title: 'LojaVents | Reserva locales en Loja'
  },
  {
    path: 'locales',
    loadComponent: () => import('./features/venues/venue-search/venue-search').then(m => m.VenueSearch),
    title: 'Explorar locales | LojaVents'
  },
  {
    path: 'locales/:id',
    loadComponent: () => import('./features/venues/venue-detail/venue-detail').then(m => m.VenueDetail),
    title: 'Detalle del local | LojaVents'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login),
    title: 'Iniciar sesión | LojaVents'
  },
  {
    path: 'registro',
    loadComponent: () => import('./features/auth/register/register').then(m => m.Register),
    title: 'Crear cuenta | LojaVents'
  },
  {
    path: 'reservar/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/booking/booking-wizard/booking-wizard').then(m => m.BookingWizard),
    title: 'Reservar local | LojaVents'
  },
  {
    path: 'favoritos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customer/favorites/favorites').then(m => m.Favorites),
    title: 'Mis favoritos | LojaVents'
  },
  {
    path: 'mis-reservas',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customer/bookings/my-bookings').then(m => m.MyBookings),
    title: 'Mis reservas | LojaVents'
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customer/profile/profile').then(m => m.Profile),
    title: 'Mi perfil | LojaVents'
  },
  {
    path: 'convertirme-en-propietario',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customer/owner-request/owner-request').then(m => m.OwnerRequest),
    title: 'Solicitar rol propietario | LojaVents'
  },
  {
    path: 'propietario',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PROPIETARIO'] },
    loadComponent: () => import('./features/owner/owner-shell/owner-shell').then(m => m.OwnerShell),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/owner/dashboard/owner-dashboard').then(m => m.OwnerDashboard)
      },
      {
        path: 'locales',
        loadComponent: () => import('./features/owner/venues/owner-venues').then(m => m.OwnerVenues)
      },
      {
        path: 'locales/nuevo',
        loadComponent: () => import('./features/owner/venue-form/venue-form').then(m => m.VenueForm)
      },
      {
        path: 'locales/:id/editar',
        loadComponent: () => import('./features/owner/venue-form/venue-form').then(m => m.VenueForm)
      },
      {
        path: 'disponibilidad',
        loadComponent: () => import('./features/owner/availability/availability').then(m => m.Availability)
      }
    ]
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./features/admin/admin-shell/admin-shell').then(m => m.AdminShell),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/admin/dashboard/admin-dashboard').then(m => m.AdminDashboard)
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./features/admin/users/admin-users').then(m => m.AdminUsers)
      },
      {
        path: 'locales',
        loadComponent: () => import('./features/admin/venues/admin-venues').then(m => m.AdminVenues)
      },
      {
        path: 'verificaciones',
        loadComponent: () => import('./features/admin/verifications/verifications').then(m => m.Verifications)
      }
    ]
  },
  {
    path: '403',
    loadComponent: () => import('./features/errors/forbidden/forbidden').then(m => m.Forbidden),
    title: 'Acceso denegado | LojaVents'
  },
  {
    path: '**',
    loadComponent: () => import('./features/errors/not-found/not-found').then(m => m.NotFound),
    title: 'Página no encontrada | LojaVents'
  }
];
