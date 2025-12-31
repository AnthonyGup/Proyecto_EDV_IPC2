import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';

const API_BASE_URL = 'http://localhost:8080/smoke';

@Component({
  selector: 'app-report-gamer-expenses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gamer-expense-history.component.html',
  styleUrls: ['./gamer-expense-history.component.css']
})
export class GamerExpenseHistoryComponent implements OnInit {
  expenses: any[] = [];
  totalSpent = 0;
  loading = false;
  error: string | null = null;

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
    this.http.get(`${API_BASE_URL}/reports/gamer-expenses?userId=${userData.mail}`).subscribe({
      next: (resp: any) => {
        this.expenses = resp.expenses || [];
        this.totalSpent = this.expenses.reduce((sum, e) => sum + e.price, 0);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los datos: ' + (err.error?.error || err.statusText);
        this.loading = false;
      }
    });
  }

  getTotalSpent(): number {
    return this.totalSpent;
  }
}
