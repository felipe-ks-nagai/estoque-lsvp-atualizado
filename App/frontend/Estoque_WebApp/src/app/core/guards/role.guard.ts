import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../authentication/authentication.service';

// Guard que libera a rota só para as roles informadas (ADMINISTRADOR sempre passa).
// Uso: canActivate: [roleGuard(ROLES.ESTOQUE)]
export const roleGuard = (...roles: string[]): CanActivateFn => () => {
  const auth = inject(AuthenticationService);
  const router = inject(Router);

  // Não logado -> login
  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  // Logado mas sem permissão -> volta para a página inicial da própria role
  if (!auth.hasRole(...roles)) {
    return router.createUrlTree([auth.getHomeRoute()]);
  }

  return true;
};
