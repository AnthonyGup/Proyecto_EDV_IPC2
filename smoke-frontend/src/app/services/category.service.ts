import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Category } from '../models/Category';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<Category[]> {
    return this.http.get<Category[]>(`${API_BASE_URL}/category/all`);
  }

  update(id: number, name: string): Observable<Category> {
    return this.http.put<Category>(`${API_BASE_URL}/category/${id}`, { name });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/category/${id}`);
  }
}
