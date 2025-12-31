import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';
import { API_BASE_URL } from '../../core/appi.config';

@Component({
  standalone: true,
  selector: 'app-report-user-ranking',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './report-user-ranking.component.html',
  styleUrls: ['./report-user-ranking.component.css']
})
export class ReportUserRankingComponent implements OnInit {
  loading = false;
  error: string | null = null;
  data: any = null;
  limit = 10;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetch();
  }

  fetch(): void {
    this.loading = true;
    this.error = null;
    let params = new HttpParams().set('limit', this.limit);
    this.http.get(`${API_BASE_URL}/reports/user-ranking`, { params }).subscribe({
      next: (resp) => {
        this.data = resp;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.error || 'Error cargando reporte';
      }
    });
  }
}
