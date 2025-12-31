import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';

@Injectable({ providedIn: 'root' })
export class RateService {
  constructor(private http: HttpClient) {}

  setRating(videogameId: number, userEmail: string, stars: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${API_BASE_URL}/rate/game/${videogameId}`,
      { userEmail, stars }
    );
  }

  getMyRating(videogameId: number, userEmail: string): Observable<{ rating: number | null }> {
    return this.http.get<{ rating: number | null }>(
      `${API_BASE_URL}/rate/game/${videogameId}`,
      { params: { userEmail } }
    );
  }
  
  getAverageRating(videogameId: number): Observable<{ average: number; count: number }> {
    return this.http.get<{ average: number; count: number }>(`${API_BASE_URL}/rate/game/${videogameId}`);
  }
}
