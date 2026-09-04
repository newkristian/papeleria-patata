import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor para manejo centralizado de respuestas de error HTTP de seguridad:
 * - 401 Unauthorized: sesión inválida o expirada. Limpia el estado de sesión y redirige a /login.
 * - 403 Forbidden: acceso denegado por permisos insuficientes.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401) {
          // No cerrar sesión en peticiones al endpoint de login mismo (para mostrar error de credenciales)
          const isLoginEndpoint = req.url.includes('/api/v1/auth/login');
          if (!isLoginEndpoint) {
            authService.logout();
          }
        }
      }
      return throwError(() => error);
    })
  );
};
