import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { API_BASE_URL } from '../../core/appi.config';

@Component({
  standalone: true,
  selector: 'app-report-global-earnings',
  imports: [CommonModule, HttpClientModule],
  templateUrl: './report-global-earnings.component.html',
  styleUrls: ['./report-global-earnings.component.css']
})
export class ReportGlobalEarningsComponent implements OnInit {
  loading = true;
  error: string | null = null;
  data: any = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetch();
  }

  fetch(): void {
    this.loading = true;
    this.error = null;
    this.http.get(`${API_BASE_URL}/reports/global-earnings`).subscribe({
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
