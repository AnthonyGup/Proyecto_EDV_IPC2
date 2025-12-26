import { Injectable } from '@angular/core';
import { User } from '../models/User';

const SESSION_KEY = 'currentUser';

@Injectable({ providedIn: 'root' })
export class SessionService {

// Guarda el usuario en el almacenamiento local
  saveUser(user: User): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
  }
// Obtiene el usuario del almacenamiento local
  getUser(): User | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }
// Elimina el usuario del almacenamiento local
  clear(): void {
    localStorage.removeItem(SESSION_KEY);
  }
}
