import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Company, GlobalCommission, User } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<User> {
    const body = { mail: email, password: password };
    return this.http.post<User>(`${API_BASE_URL}/loggin`, body); 
  }

  registerGamer(gamerData: any): Observable<any> {
    return this.http.post(`${API_BASE_URL}/gamer/creator`, gamerData);
  }

  createCompany(payload: Pick<Company, 'name' | 'description' | 'commission'>): Observable<Company> {
    return this.http.post<Company>(`${API_BASE_URL}/company/create`, payload);
  }

  getGlobalCommission(): Observable<GlobalCommission> {
    return this.http.get<GlobalCommission>(`${API_BASE_URL}/CommissionController`);
  }

  updateGlobalCommission(commission: number): Observable<{ message: string }> {
    const payload = { commission: Number(commission) };
    return this.http.post<{ message: string }>(`${API_BASE_URL}/CommissionController`, payload);
  }

  createCompanyAdmin(admin: {
    mail: string;
    nickname: string;
    password: string;
    birthdate: string; // yyyy-mm-dd
    type: 'COMPANY_ADMIN';
    company_id: number;
  }): Observable<any> {
    return this.http.post(`${API_BASE_URL}/user/company`, admin);
  }

  getUserCompanyInfo(userEmail: string): Observable<{ company_id: number; user_id: string }> {
    return this.http.get<{ company_id: number; user_id: string }>(`${API_BASE_URL}/user/company/info/${userEmail}`);
  }
}