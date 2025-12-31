import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';
import { AuthService } from '../../services/auth.service';

const API_BASE_URL = 'http://localhost:8080/smoke';

@Component({
  selector: 'app-report-company-top-games',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './company-top-games-report.component.html',
  styleUrls: ['./company-top-games-report.component.css']
})
export class CompanyTopGamesReportComponent implements OnInit {
  companyId: number | null = null;
  startDate: string = '';
  endDate: string = '';
  limit = 5;
  topGames: any[] = [];
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
    } else if (user?.mail) {
      this.authService.getUserCompanyInfo(user.mail).subscribe({
        next: (info) => {
          const cid = info.company_id;
          this.companyId = cid;
          this.user = { ...user, company_id: cid };
          this.sessionService.saveUser(this.user);
        },
        error: () => {
          this.error = 'No se pudo obtener la compañía del usuario.';
        }
      });
    }
  }

  fetch(): void {
    if (!this.companyId || !this.startDate || !this.endDate) {
      this.topGames = [];
      return;
    }

    this.loading = true;
    this.error = null;
    const params = `companyId=${this.companyId}&startDate=${this.startDate}&endDate=${this.endDate}&limit=${this.limit}`;
    this.http.get(`${API_BASE_URL}/reports/company-top-games?${params}`).subscribe({
      next: (resp: any) => {
        this.topGames = resp.topGames || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los datos: ' + (err.error?.error || err.statusText);
        this.loading = false;
      }
    });
  }
}
