import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { Category, Company, Videogame } from '../../models';
import { SessionService } from '../../core/session.service';
import { CategoryService } from '../../services/category.service';
import { CompanyService } from '../../services/company.service';
import { VideogameService } from '../../services/videogame.service';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-videogame-create',
  templateUrl: './videogame-create.component.html',
  styleUrls: ['./videogame-create.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class VideogameCreateComponent implements OnInit {
  form!: FormGroup;
  categories: Category[] = [];
  selectedCategoryIds: Set<number> = new Set<number>();
  files: File[] = [];

  companyName: string = '';
  loadingCompany = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  user: any | null = null;

  constructor(
    private fb: FormBuilder,
    private session: SessionService,
    private categoryService: CategoryService,
    private companyService: CompanyService,
    private authService: AuthService,
    private videogameService: VideogameService
  ) {
    this.form = this.fb.group({
      companyId: new FormControl('', [Validators.required]),
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: [0, [Validators.required, Validators.min(0)]],
      relasedate: ['', [Validators.required]],
      minimRequirements: ['', [Validators.required, Validators.minLength(5)]],
      ageRestriction: [0, [Validators.required, Validators.min(0)]],
      available: [true]
    });

    this.user = this.session.getUser();
  }

  ngOnInit(): void {
    // Cargar categorías
    this.categoryService.getAll().subscribe({
      next: (cats) => (this.categories = cats || []),
      error: () => (this.errorMessage = 'Error al cargar categorías')
    });

    // Company admin: usar su propia compañía
    const user = this.user;
    if (user?.company_id) {
      this.form.patchValue({ companyId: user.company_id });
      this.fetchCompanyName(user.company_id);
    } else if (user?.mail) {
      this.loadingCompany = true;
      this.authService.getUserCompanyInfo(user.mail)
        .pipe(finalize(() => (this.loadingCompany = false)))
        .subscribe({
          next: (info) => {
            const cid = info.company_id;
            this.user = { ...user, company_id: cid };
            this.session.saveUser(this.user);
            this.form.patchValue({ companyId: cid });
            this.fetchCompanyName(cid);
          },
          error: () => (this.errorMessage = 'No se pudo obtener la compañía del usuario')
        });
    }
  }

  fetchCompanyName(companyId: number): void {
    this.companyService.getCompanyById(companyId).subscribe({
      next: (company) => {
        this.companyName = company?.name || '';
      }
    });
  }

  toggleCategory(id: number, checked: boolean): void {
    if (checked) this.selectedCategoryIds.add(id);
    else this.selectedCategoryIds.delete(id);
  }

  onFilesSelected(evt: Event): void {
    const input = evt.target as HTMLInputElement;
    const list = input.files;
    this.files = [];
    if (list && list.length) {
      for (let i = 0; i < list.length; i++) this.files.push(list.item(i)!);
    }
  }

  get f() { return this.form.controls; }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = this.form.value as Partial<Videogame> & { companyId: number };
    const categoryIds = Array.from(this.selectedCategoryIds);

    this.videogameService.createGame(payload, categoryIds, this.files)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Juego creado exitosamente';
          this.form.reset({ available: true });
          this.selectedCategoryIds.clear();
          this.files = [];
        },
        error: (err) => {
          this.errorMessage = err?.error?.error || 'Error creando el juego';
        }
      });
  }
}
