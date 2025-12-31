import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/Category';

@Component({
  standalone: true,
  selector: 'app-gamer-navbar',
  templateUrl: './gamer-navbar.component.html',
  styleUrls: ['./gamer-navbar.component.css'],
  imports: [CommonModule, RouterModule, FormsModule]
})
export class GamerNavbarComponent implements OnInit {
  showFilters = false;
  showFamilyDropdown = false;
  showReportsDropdown = false;
  isFamilyDropdownPinned = false;
  searchName = '';
  filterAvailable: boolean | undefined;
  filterCategoryId: number | undefined;
  filterMinPrice: number | undefined;
  filterMaxPrice: number | undefined;
  filterMaxAge: number | undefined;
  categories: Category[] = [];
  companyQuery = '';
  gamerQuery = '';

  constructor(
    private session: SessionService,
    private router: Router,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (cats: Category[]) => {
        this.categories = cats;
      },
      error: (err: any) => {
        console.error('Error loading categories:', err);
      }
    });
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  toggleReportsDropdown(): void {
    this.showReportsDropdown = !this.showReportsDropdown;
  }

  toggleFamilyDropdown(): void {
    this.isFamilyDropdownPinned = !this.isFamilyDropdownPinned;
    this.showFamilyDropdown = this.isFamilyDropdownPinned;
  }

  closeFamilyDropdown(): void {
    // Sólo cerrar si no está fijado/pinned
    if (!this.isFamilyDropdownPinned) {
      this.showFamilyDropdown = false;
    }
  }

  openFamilyDropdown(): void {
    // Abrir por hover sólo si no está fijado
    if (!this.isFamilyDropdownPinned) {
      this.showFamilyDropdown = true;
    }
  }

  unpinAndCloseFamilyDropdown(): void {
    this.isFamilyDropdownPinned = false;
    this.showFamilyDropdown = false;
  }

  performSearch(): void {
    const filters: any = {
      name: this.searchName || undefined,
      available: this.filterAvailable,
      categoryId: this.filterCategoryId,
      minPrice: this.filterMinPrice,
      maxPrice: this.filterMaxPrice,
      maxAge: this.filterMaxAge
    };

    Object.keys(filters).forEach(key => filters[key] === undefined && delete filters[key]);

    if (Object.keys(filters).length > 0) {
      this.router.navigate(['/gamer/search'], { queryParams: filters });
      this.clearSearch();
    }
  }

  clearSearch(): void {
    this.searchName = '';
    this.filterAvailable = undefined;
    this.filterCategoryId = undefined;
    this.filterMinPrice = undefined;
    this.filterMaxPrice = undefined;
    this.filterMaxAge = undefined;
    this.showFilters = false;
  }

  searchCompanies(): void {
    const q = (this.companyQuery || '').trim();
    if (!q) return;
    this.router.navigate(['/gamer/company/search'], { queryParams: { q } });
    this.companyQuery = '';
  }

  searchGamers(): void {
    const q = (this.gamerQuery || '').trim();
    if (!q) return;
    this.router.navigate(['/gamer/gamers/search'], { queryParams: { q } });
    this.gamerQuery = '';
  }

  goHome(): void {
    this.router.navigate(['/gamer']);
  }

  logout(): void {
    this.session.clear();
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown-container') && !target.closest('.reports-dropdown-container')) {
      this.showReportsDropdown = false;
    }
  }
}

