import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Comment } from '../models';

@Injectable({ providedIn: 'root' })
export class CommentService {
  constructor(private http: HttpClient) {}

  updateCommentsVisibility(companyId: number, visible: boolean): Observable<{ message: string; visible: boolean }> {
    return this.http.put<{ message: string; visible: boolean }>(
      `${API_BASE_URL}/comments/visibility?company_id=${companyId}&visible=${visible}`,
      {}
    );
  }

  getGameCommentsStatus(videogameId: number): Observable<{ enabled: boolean }> {
    return this.http.get<{ enabled: boolean }>(
      `${API_BASE_URL}/comments/game/${videogameId}/status`
    );
  }

  disableGameComments(videogameId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${API_BASE_URL}/comments/game/${videogameId}/disable`,
      {}
    );
  }

  enableGameComments(videogameId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${API_BASE_URL}/comments/game/${videogameId}/enable`,
      {}
    );
  }

  getCommentsByGame(videogameId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(
      `${API_BASE_URL}/comments/game/${videogameId}`
    );
  }
}
