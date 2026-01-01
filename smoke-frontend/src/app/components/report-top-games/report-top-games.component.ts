import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';
import { API_BASE_URL } from '../../core/appi.config';

@Component({
  standalone: true,
  selector: 'app-report-top-games',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './report-top-games.component.html',
  styleUrls: ['./report-top-games.component.css']
})
export class ReportTopGamesComponent implements OnInit {
  loading = false;
  error: string | null = null;
  data: any = null;

  sortBy: 'sales' | 'rating' = 'sales';
  categoryId: number | null = null;
  ageRestriction: number | null = null;
  limit = 10;

  categories: any[] = [];
  loadingCategories = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadCategories();
    this.fetch();
  }

  loadCategories(): void {
    this.loadingCategories = true;
    this.http.get(`${API_BASE_URL}/category/all`).subscribe({
      next: (resp: any) => {
        this.categories = resp || [];
        this.loadingCategories = false;
      },
      error: () => {
        this.loadingCategories = false;
      }
    });
  }

  fetch(): void {
    this.loading = true;
    this.error = null;

    let params = new HttpParams().set('sortBy', this.sortBy).set('limit', this.limit);
    if (this.categoryId) params = params.set('categoryId', this.categoryId);
    if (this.ageRestriction) params = params.set('ageRestriction', this.ageRestriction);

    this.http.get(`${API_BASE_URL}/reports/top-games`, { params }).subscribe({
      next: (resp: any) => {
        this.data = resp?.data || resp;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.error || 'Error cargando reporte';
      }
    });
  }

  exportToPDF(): void {
    let url = `${API_BASE_URL}/reports/top-games/export?sortBy=${this.sortBy}&limit=${this.limit}`;
    if (this.categoryId) {
      url += `&categoryId=${this.categoryId}`;
    }
    if (this.ageRestriction) {
      url += `&ageRestriction=${this.ageRestriction}`;
    }
    window.open(url, '_blank');
  }
}
