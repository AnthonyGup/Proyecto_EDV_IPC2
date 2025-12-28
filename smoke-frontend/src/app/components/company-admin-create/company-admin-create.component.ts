import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { CompanyService } from '../../services/company.service';
import { SessionService } from '../../core/session.service';
import { extractErrorMessage } from '../../core/error.util';
import { Company } from '../../models';

@Component({
  standalone: true,
  selector: 'app-company-admin-create',
  templateUrl: './company-admin-create.component.html',
  styleUrls: ['./company-admin-create.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class CompanyAdminCreateComponent implements OnInit {
  form!: FormGroup;
  companyName: string = '';
  loadingCompany = false;
  isSystemAdmin = false;
  companies: Company[] = [];
  loadingCompanies = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  user: any | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private companyService: CompanyService,
    private session: SessionService
  ) {
    this.form = this.fb.group({
      company_id: new FormControl('', [Validators.required]),
      mail: ['', [Validators.required, Validators.email]],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]],
      birthdate: ['', [Validators.required]]
    });

    this.user = this.session.getUser();
    this.isSystemAdmin = this.user?.type === 'SYSTEM_ADMIN';
  }

  ngOnInit(): void {
    const user = this.user;
    if (this.isSystemAdmin) {
      // Sistema puede elegir cualquier compañía
      this.loadCompanies();
    } else {
      // Company admin: fija su propia compañía
      if (user?.company_id) {
        this.form.patchValue({ company_id: user.company_id });
        this.fetchCompanyName(user.company_id);
      } else if (user?.mail) {
        this.loadingCompany = true;
        this.auth.getUserCompanyInfo(user.mail)
          .pipe(finalize(() => (this.loadingCompany = false)))
          .subscribe({
            next: (info) => {
              const cid = info.company_id;
              this.user = { ...user, company_id: cid };
              this.session.saveUser(this.user);
              this.form.patchValue({ company_id: cid });
              this.fetchCompanyName(cid);
            },
            error: () => {
              this.errorMessage = 'No se pudo obtener la compañía del usuario.';
            }
          });
      }
    }
  }

  fetchCompanyName(companyId: number): void {
    this.companyService.getCompanyById(companyId).subscribe({
      next: (company) => {
        this.companyName = company?.name || '';
      }
    });
  }

  loadCompanies(): void {
    this.loadingCompanies = true;
    this.companyService.getAllCompanies()
      .pipe(finalize(() => (this.loadingCompanies = false)))
      .subscribe({
        next: (companies) => {
          this.companies = companies || [];
        },
        error: () => {
          this.errorMessage = 'Error al cargar las compañías.';
        }
      });
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

    const { company_id, mail, nickname, password, birthdate } = this.form.value as any;

    const payload: {
      mail: string;
      nickname: string;
      password: string;
      birthdate: string;
      type: 'COMPANY_ADMIN';
      company_id: number;
    } = {
      mail: String(mail),
      nickname: String(nickname),
      password: String(password),
      birthdate: String(birthdate),
      type: 'COMPANY_ADMIN',
      company_id: Number(company_id)
    };

    this.auth.createCompanyAdmin(payload)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Administrador de compañía creado exitosamente.';
          this.form.reset();
        },
        error: (err) => {
          this.errorMessage = extractErrorMessage(err);
        }
      });
  }
}
