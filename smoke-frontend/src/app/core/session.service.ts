import { Injectable } from '@angular/core';
import { User } from '../models/User';

const SESSION_KEY = 'currentUser';

@Injectable({ providedIn: 'root' })
export class SessionService {

// Guarda el usuario en la sesión del navegador
  saveUser(user: User): void {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));
  }
// Obtiene el usuario de la sesión del navegador
  getUser(): User | null {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }
// Elimina el usuario de la sesión del navegador
  clear(): void {
    sessionStorage.removeItem(SESSION_KEY);
  }

  isAuthenticated(): boolean {
    return this.getUser() !== null;
  }
}
