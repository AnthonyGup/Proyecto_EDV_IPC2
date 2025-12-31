import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models';
import { extractErrorMessage } from '../../core/error.util';

@Component({
  standalone: true,
  selector: 'app-company-commissions',
  templateUrl: './company-commissions.component.html',
  styleUrls: ['./company-commissions.component.css'],
  imports: [CommonModule, FormsModule]
})
export class CompanyCommissionsComponent implements OnInit {
  companies: Company[] = [];
  loading = false;
  errorMessage = '';
  
  editingCompany: Company | null = null;
  newCommission: number = 0;
  showModal = false;
  saveLoading = false;
  saveMessage = '';

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
    this.editingCompany = company;
    this.newCommission = company.commission * 100; // Convert to percentage
    this.showModal = true;
    this.saveMessage = '';
  }

  closeModal(): void {
    this.showModal = false;
    this.editingCompany = null;
    this.saveMessage = '';
  }

  saveCommission(): void {
    if (!this.editingCompany || this.newCommission < 0 || this.newCommission > 100) {
      this.saveMessage = 'La comisión debe estar entre 0 y 100%';
      return;
    }

    this.saveLoading = true;
    this.saveMessage = '';
    
    const commissionDecimal = this.newCommission / 100;
    
    this.companyService.updateCommission(this.editingCompany.companyId, commissionDecimal)
      .pipe(finalize(() => (this.saveLoading = false)))
      .subscribe({
        next: (updatedCompany) => {
          this.saveMessage = 'Comisión actualizada exitosamente';
          // Update local data
          if (this.editingCompany) {
            this.editingCompany.commission = updatedCompany.commission;
          }
          setTimeout(() => {
            this.closeModal();
            this.loadCompanies();
          }, 1500);
        },
        error: (err) => {
          this.saveMessage = 'Error: ' + extractErrorMessage(err);
        }
      });
  }
}
