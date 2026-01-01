import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';
import { AuthService } from '../../services/auth.service';

const API_BASE_URL = 'http://localhost:8080/smoke';

@Component({
  selector: 'app-report-company-sales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './company-sales-report.component.html',
  styles: [`
    h3 { font-weight: bold; }
    .table { font-size: 0.9rem; }
  `]
})
export class CompanySalesReportComponent implements OnInit {
  companyId: number | null = null;
  sales: any[] = [];
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
      this.sales = [];
      return;
    }

    this.loading = true;
    this.error = null;
    this.http.get(`${API_BASE_URL}/reports/company-sales?companyId=${this.companyId}`).subscribe({
      next: (resp: any) => {
        this.sales = resp.sales || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los datos: ' + (err.error?.error || err.statusText);
        this.loading = false;
      }
    });
  }

  exportToPDF(): void {
    if (!this.companyId) {
      alert('Por favor, carga los datos primero');
      return;
    }

    this.loading = true;
    this.http.get(`${API_BASE_URL}/reports/company-sales/export?companyId=${this.companyId}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        this.loading = false;
      },
      error: (err) => {
        alert('Error al exportar el reporte: ' + (err.error?.error || err.statusText));
        this.loading = false;
      }
    });
  }
}
