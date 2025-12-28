import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SessionService } from '../../core/session.service';
import { CompanyService } from '../../services/company.service';
import { AuthService } from '../../services/auth.service';
import { Company } from '../../models';
import { switchMap } from 'rxjs/operators';
import { FooterComponent } from '../footer/footer.component';

@Component({
  standalone: true,
  selector: 'app-company-admin-layout',
  templateUrl: './company-admin-layout.component.html',
  styleUrls: ['./company-admin-layout.component.css'],
  imports: [CommonModule, RouterModule, FooterComponent]
})
export class CompanyAdminLayoutComponent implements OnInit {
  user: any = null;
  company: Company | null = null;
  loading = true;

  constructor(
    private session: SessionService, 
    private companyService: CompanyService,
    private authService: AuthService,
    private router: Router
  ) {
    this.user = this.session.getUser();
  }

  ngOnInit(): void {
    if (this.user && this.user.mail) {
      // Obtener company_id primero
      this.authService.getUserCompanyInfo(this.user.mail).pipe(
        switchMap(info => {
          // Guardar company_id en el usuario de sesión
          this.user.company_id = info.company_id;
          this.session.saveUser(this.user);
          // Luego obtener la info de la compañía
          return this.companyService.getCompanyById(info.company_id);
        })
      ).subscribe({
        next: (company) => {
          this.company = company;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error al cargar información:', err);
          this.loading = false;
        }
      });
    } else {
      this.loading = false;
    }
  }

  logout(): void {
    this.session.clear();
    this.router.navigate(['/login']);
  }

  goHome(): void {
    this.router.navigate(['/company/admin']);
  }
}

