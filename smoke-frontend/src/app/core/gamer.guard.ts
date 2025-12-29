import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

export const gamerGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  const user = session.getUser();

  if (user && user.type === 'GAMER') {
    return true;
  }

  return router.parseUrl('/login');
};
