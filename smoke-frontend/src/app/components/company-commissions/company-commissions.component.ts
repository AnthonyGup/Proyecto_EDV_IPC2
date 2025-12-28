import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models';
import { extractErrorMessage } from '../../core/error.util';

@Component({
  standalone: true,
  selector: 'app-company-commissions',
  templateUrl: './company-commissions.component.html',
  styleUrls: ['./company-commissions.component.css'],
  imports: [CommonModule]
})
export class CompanyCommissionsComponent implements OnInit {
  companies: Company[] = [];
  loading = false;
  errorMessage = '';

  constructor(private companyService: CompanyService) {}

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.companyService
      .getAllCompanies()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (companies) => {
          this.companies = companies || [];
        },
        error: (err) => {
          this.errorMessage = extractErrorMessage(err);
        }
      });
  }

  editCommission(company: Company): void {
    // TODO: implementar la funcionalidad de edición
    console.log('Editar comisión de:', company.name);
  }
}
