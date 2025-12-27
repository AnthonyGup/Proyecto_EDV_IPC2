import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { SessionService } from '../../core/session.service';
import { extractErrorMessage } from '../../core/error.util';

@Component({
  standalone: true,
  selector: 'app-company-admin-create',
  templateUrl: './company-admin-create.component.html',
  styleUrls: ['./company-admin-create.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class CompanyAdminCreateComponent {
  form!: FormGroup;

  loading = false;
  errorMessage = '';
  successMessage = '';

  user: any | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService, private session: SessionService) {
    this.form = this.fb.group({
      mail: ['', [Validators.required, Validators.email]],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]],
      birthdate: ['', [Validators.required]]
    });

    this.user = this.session.getUser();
  }

  get f() { return this.form.controls; }

  submit(): void {
    if (!this.user || this.user.type !== 'COMPANY_ADMIN') {
      this.errorMessage = 'Acceso restringido.';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const companyId = (this.user as any).company_id ?? (this.user as any).companyId;
    if (!companyId) {
      this.errorMessage = 'No se encontró el company_id en la sesión.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { mail, nickname, password, birthdate } = this.form.value as any;

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
      company_id: Number(companyId)
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
