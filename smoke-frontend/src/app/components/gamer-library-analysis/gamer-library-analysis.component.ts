import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';

const API_BASE_URL = 'http://localhost:8080/smoke';

@Component({
  selector: 'app-report-gamer-library',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gamer-library-analysis.component.html',
  styleUrls: ['./gamer-library-analysis.component.css']
})
export class GamerLibraryAnalysisComponent implements OnInit {
  library: any[] = [];
  favoriteCategories: any[] = [];
  loading = false;
  error: string | null = null;
  data: any = {};

  constructor(private http: HttpClient, private sessionService: SessionService) {}

  ngOnInit(): void {
    this.fetch();
  }

  fetch(): void {
    const userData = this.sessionService.getUser();
    if (!userData || !userData.mail) {
      this.error = 'Usuario no autenticado';
      return;
    }

    this.loading = true;
    this.error = null;
    this.http.get(`${API_BASE_URL}/reports/gamer-library-analysis?userId=${userData.mail}`).subscribe({
      next: (resp: any) => {
        this.library = resp.library || [];
        this.favoriteCategories = resp.favoriteCategories || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los datos: ' + (err.error?.error || err.statusText);
        this.loading = false;
      }
    });
  }

  getDifferenceClass(diff: number): string {
    if (diff > 0) return 'positive';
    if (diff < 0) return 'negative';
    return '';
  }
}
