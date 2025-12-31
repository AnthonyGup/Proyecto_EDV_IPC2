import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Company, Videogame } from '../models';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  constructor(private http: HttpClient) { }

  getAllCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>(`${API_BASE_URL}/company/all`);
  }

  getCompanyById(id: number): Observable<Company> {
    return this.http.get<Company>(`${API_BASE_URL}/company/${id}`);
  }

  getCompanyGames(companyId: number): Observable<Videogame[]> {
    return this.http.get<Videogame[]>(`${API_BASE_URL}/company/games/${companyId}`);
  }

  searchCompanies(q: string): Observable<Company[]> {
    return this.http.get<Company[]>(`${API_BASE_URL}/company/search`, { params: { q } });
  }

  updateCommission(companyId: number, commission: number): Observable<Company> {
    return this.http.put<Company>(`${API_BASE_URL}/company/${companyId}`, { 
      companyId: companyId,
      commission: commission 
    });
  }
}
