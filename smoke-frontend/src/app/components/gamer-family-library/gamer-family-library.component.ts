import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';

const API_BASE_URL = 'http://localhost:8080/smoke';

@Component({
  selector: 'app-report-gamer-family',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gamer-family-library.component.html',
  styleUrls: ['./gamer-family-library.component.css']
})
export class GamerFamilyLibraryComponent implements OnInit {
  familyGroupId: number | null = null;
  sharedGames: any[] = [];
  loading = false;
  error: string | null = null;
  data: any = null;

  constructor(private http: HttpClient, private sessionService: SessionService) {}

  ngOnInit(): void {}

  fetch(): void {
    if (!this.familyGroupId) {
      this.sharedGames = [];
      return;
    }

    this.loading = true;
    this.error = null;
    const userData = this.sessionService.getUser();
    const userId = userData?.mail || '';
    const params = `familyGroupId=${this.familyGroupId}${userId ? '&userId=' + userId : ''}`;
    
    this.http.get(`${API_BASE_URL}/reports/gamer-family-library?${params}`).subscribe({
      next: (resp: any) => {
        this.sharedGames = resp.sharedGames || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los datos: ' + (err.error?.error || err.statusText);
        this.loading = false;
      }
    });
  }
}
