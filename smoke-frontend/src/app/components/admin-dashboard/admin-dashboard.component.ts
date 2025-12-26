import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SessionService } from '../../core/session.service';

@Component({
  standalone: true,
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  imports: [CommonModule]
})
export class AdminDashboardComponent {
  user!: ReturnType<SessionService['getUser']>;

  constructor(private session: SessionService) {
    this.user = this.session.getUser();
  }
}