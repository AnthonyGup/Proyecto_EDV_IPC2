import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Videogame } from '../models';

@Injectable({ providedIn: 'root' })
export class LibraryService {
  constructor(private http: HttpClient) {}

  getUserLibrary(userEmail: string, filterType?: string, filterValue?: string): Observable<Videogame[]> {
    const params: string[] = [`userId=${encodeURIComponent(userEmail)}`];
    if (filterType) params.push(`filterType=${encodeURIComponent(filterType)}`);
    if (filterValue) params.push(`filterValue=${encodeURIComponent(filterValue)}`);
    const qs = params.length ? `?${params.join('&')}` : '';
    return this.http.get<Videogame[]>(`${API_BASE_URL}/library/games${qs}`);
  }

  installGame(userEmail: string, gameId: number): Observable<{ message: string }> {
    const payload = {
      gamer: { mail: userEmail },
      game: { videogameId: gameId }
    };
    return this.http.post<{ message: string }>(`${API_BASE_URL}/game/install`, payload);
  }

  uninstallGame(userEmail: string, gameId: number): Observable<{ message: string }> {
    const payload = {
      gamer: { mail: userEmail },
      game: { videogameId: gameId }
    };
    return this.http.post<{ message: string }>(`${API_BASE_URL}/game/uninstall`, payload);
  }

  getInstallStatus(userEmail: string, gameId: number): Observable<{ inLibrary: boolean; installed: boolean; buyed: boolean }> {
    return this.http.get<{ inLibrary: boolean; installed: boolean; buyed: boolean }>(
      `${API_BASE_URL}/library/status?userId=${encodeURIComponent(userEmail)}&gameId=${gameId}`
    );
  }
}
