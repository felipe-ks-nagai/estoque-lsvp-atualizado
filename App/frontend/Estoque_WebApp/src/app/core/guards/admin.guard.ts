import { CanActivateFn } from '@angular/router';
import { roleGuard } from './role.guard';

// Mantido por compatibilidade: agora é só um atalho para o roleGuard sem roles extras
// (ou seja, apenas ADMINISTRADOR passa)
export const adminGuard: CanActivateFn = roleGuard();
