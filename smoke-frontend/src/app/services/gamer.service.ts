import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';

export interface GamerSummary {
  mail: string;
  nickname: string;
  country?: string;
  phone?: number;
}

export interface GamerPublicInfo {
  userEmail: string;
  nickname: string;
  country?: string;
  phone?: number;
}

@Injectable({ providedIn: 'root' })
export class GamerService {
  constructor(private http: HttpClient) {}

  searchGamers(q: string): Observable<GamerSummary[]> {
    const params = new HttpParams().set('q', q);
    return this.http.get<GamerSummary[]>(`${API_BASE_URL}/gamer/search`, { params });
    }

  getGamerInfo(email: string): Observable<GamerPublicInfo> {
    return this.http.get<GamerPublicInfo>(`${API_BASE_URL}/gamer/info/${email}`);
  }
}
