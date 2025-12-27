import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SessionService } from '../../core/session.service';

@Component({
  standalone: true,
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  imports: [CommonModule, RouterModule]
})
export class AdminDashboardComponent {
  user: ReturnType<SessionService['getUser']> = null;

  constructor(private session: SessionService) {
    this.user = this.session.getUser();
  }
}