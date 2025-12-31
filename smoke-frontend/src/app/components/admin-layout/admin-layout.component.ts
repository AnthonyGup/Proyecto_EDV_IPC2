import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SessionService } from '../../core/session.service';
import { FooterComponent } from '../footer/footer.component';

@Component({
  standalone: true,
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css'],
  imports: [CommonModule, RouterModule, FooterComponent]
})
export class AdminLayoutComponent {
  showCompanyDropdown = false;
  showCategoryDropdown = false;
  showReports = false;

  constructor(private session: SessionService, private router: Router) {}

  goHome(): void {
    this.router.navigate(['/admin']);
  }

  toggleCompanyDropdown(): void {
    this.showCompanyDropdown = !this.showCompanyDropdown;
  }

  toogleCategoryDropdown(): void {
    this.showCategoryDropdown = !this.showCategoryDropdown;
  }

  toggleReportsDropdown(): void {
    this.showReports = !this.showReports;
  }

  logout(): void {
    this.session.clear();
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInsideDropdown = !!target.closest('.dropdown-container');
    if (!clickedInsideDropdown) {
      this.showCompanyDropdown = false;
      this.showCategoryDropdown = false;
      this.showReports = false;
    }
  }
}
