import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SessionService } from '../../core/session.service';

@Component({
  standalone: true,
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css'],
  imports: [CommonModule, RouterModule]
})
export class AdminLayoutComponent {
  constructor(private session: SessionService, private router: Router) {}

  logout(): void {
    this.session.clear();
    this.router.navigate(['/login']);
  }
}
