import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { API_BASE_URL } from '../../core/appi.config';
import { SessionService } from '../../core/session.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-report-company-feedback',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './report-company-feedback.component.html',
  styleUrls: ['./report-company-feedback.component.css']
})
export class ReportCompanyFeedbackComponent implements OnInit {
  companyId: number | null = null;
  limit = 10;
  averageRatings: any[] = [];
  topComments: any[] = [];
  worstRated: any[] = [];
  loading = false;
  error: string | null = null;
  user: any = null;

  constructor(
    private http: HttpClient, 
    private sessionService: SessionService,
    private authService: AuthService
  ) {
    this.user = this.sessionService.getUser();
  }

  ngOnInit(): void {
    const user = this.user;
    if (user?.company_id) {
      this.companyId = user.company_id;
      this.fetch();
    } else if (user?.mail) {
      this.authService.getUserCompanyInfo(user.mail).subscribe({
        next: (info) => {
          const cid = info.company_id;
          this.companyId = cid;
          this.user = { ...user, company_id: cid };
          this.sessionService.saveUser(this.user);
          this.fetch();
        },
        error: () => {
          this.error = 'No se pudo obtener la compañía del usuario.';
        }
      });
    }
  }

  fetch(): void {
    if (!this.companyId) {
      this.averageRatings = [];
      this.topComments = [];
      this.worstRated = [];
      return;
    }

    this.loading = true;
    this.error = null;
    this.http.get(`${API_BASE_URL}/reports/company-feedback?companyId=${this.companyId}&limit=${this.limit}`).subscribe({
      next: (resp: any) => {
        this.averageRatings = resp.averageRatings || [];
        this.topComments = resp.topComments || [];
        this.worstRated = resp.worstRated || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los datos: ' + (err.error?.error || err.statusText);
        this.loading = false;
      }
    });
  }

  exportRatingsToPDF(): void {
    if (!this.companyId) {
      alert('Por favor, carga los datos primero');
      return;
    }
    
    this.loading = true;
    this.http.get(`${API_BASE_URL}/reports/company-ratings/export?companyId=${this.companyId}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        this.loading = false;
      },
      error: (err) => {
        alert('Error al generar el PDF de calificaciones');
        this.loading = false;
      }
    });
  }

  exportCommentsToPDF(): void {
    if (!this.companyId) {
      alert('Por favor, carga los datos primero');
      return;
    }
    
    this.loading = true;
    this.http.get(`${API_BASE_URL}/reports/company-comments/export?companyId=${this.companyId}&limit=${this.limit}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        this.loading = false;
      },
      error: (err) => {
        alert('Error al generar el PDF de comentarios');
        this.loading = false;
      }
    });
  }

  exportWorstGamesToPDF(): void {
    if (!this.companyId) {
      alert('Por favor, carga los datos primero');
      return;
    }
    
    this.loading = true;
    this.http.get(`${API_BASE_URL}/reports/company-worst-games/export?companyId=${this.companyId}&limit=${this.limit}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        this.loading = false;
      },
      error: (err) => {
        alert('Error al generar el PDF de juegos peor calificados');
        this.loading = false;
      }
    });
  }
}
