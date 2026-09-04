import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RolUsuario } from '../models/usuario-sesion.model';

/**
 * Guard para autorizar el acceso a rutas según los roles requeridos.
 * - Si no está autenticado: redirige a /login.
 * - Si está autenticado pero no tiene ninguno de los roles permitidos: redirige a /pos.
 */
export function roleGuard(allowedRoles: RolUsuario[]): CanActivateFn {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    if (authService.hasAnyRole(allowedRoles)) {
      return true;
    }

    return router.createUrlTree(['/pos']);
  };
}
