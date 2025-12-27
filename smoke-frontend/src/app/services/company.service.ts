import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Company } from '../models';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  constructor(private http: HttpClient) { }

  getCompanyById(id: number): Observable<Company> {
    return this.http.get<Company>(`${API_BASE_URL}/company/${id}`);
  }
}
