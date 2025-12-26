import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<any> {
    const body = { mail: email, password: password };
    return this.http.post(`${API_BASE_URL}/loggin`, body); 
  }

  registerGamer(gamerData: any): Observable<any> {
    return this.http.post(`${API_BASE_URL}/gamer/creator`, gamerData);
  }
}