import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const userId = localStorage.getItem('lojavents-session-user-id');
  const cloned = userId
    ? request.clone({ setHeaders: { 'X-Demo-User-Id': userId } })
    : request;
  return next(cloned);
};
