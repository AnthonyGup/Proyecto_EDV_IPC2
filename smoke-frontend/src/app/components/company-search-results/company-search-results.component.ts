import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { Company } from '../../models';
import { CompanyService } from '../../services/company.service';

@Component({
  standalone: true,
  selector: 'app-company-search-results',
  templateUrl: './company-search-results.component.html',
  styleUrls: ['./company-search-results.component.css'],
  imports: [CommonModule, RouterModule]
})
export class CompanySearchResultsComponent implements OnInit {
  results: Company[] = [];
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private companyService: CompanyService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const q = (params['q'] || '').trim();
      if (!q) {
        this.results = [];
        this.error = '';
        this.loading = false;
        return;
      }
      this.performSearch(q);
    });
  }

  performSearch(q: string): void {
    this.loading = true;
    this.error = '';
    this.companyService.searchCompanies(q).subscribe({
      next: (companies: Company[]) => {
        this.results = companies;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Company search error', err);
        this.error = 'Error al buscar compañías';
        this.loading = false;
      }
    });
  }
}
