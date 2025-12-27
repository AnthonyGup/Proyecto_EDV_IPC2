import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { finalize, switchMap } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { extractErrorMessage } from '../../core/error.util';
import { Company } from '../../models/Company';

@Component({
  standalone: true,
  selector: 'app-company-create',
  templateUrl: './company-create.component.html',
  styleUrls: ['./company-create.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class CompanyCreateComponent implements OnInit {
  form!: FormGroup;

  loading = false;
  errorMessage = '';
  successMessage = '';
  defaultCommission: number | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      commission: [null, [Validators.min(0), Validators.max(100)]],
      adminMail: ['', [Validators.required, Validators.email]],
      adminNickname: ['', [Validators.required, Validators.minLength(3)]],
      adminPassword: ['', [Validators.required, Validators.minLength(3)]],
      adminBirthdate: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.auth.getGlobalCommission().subscribe({
      next: (gc) => {
        this.defaultCommission = Number(gc?.commission ?? 0.15);
        // Prellenar el campo comisión con el valor global
        this.form.patchValue({ commission: this.defaultCommission });
      },
      error: () => {
        this.defaultCommission = 0.15;
        this.form.patchValue({ commission: this.defaultCommission });
      }
    });
  }

  get f() {
    return this.form.controls;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { name, description, commission, adminMail, adminNickname, adminPassword, adminBirthdate } = this.form.value as any;

    let commissionValue = commission != null ? Number(commission) : (this.defaultCommission ?? 0.15);
    if (isNaN(commissionValue) || commissionValue < 0) {
      commissionValue = this.defaultCommission ?? 0.15;
    }

    const payload = {
      name: String(name),
      description: String(description),
      commission: commissionValue
    } as Pick<Company, 'name' | 'description' | 'commission'>;

    this.auth
      .createCompany(payload)
      .pipe(
        switchMap((createdCompany) =>
          this.auth.createCompanyAdmin({
            mail: String(adminMail),
            nickname: String(adminNickname),
            password: String(adminPassword),
            birthdate: String(adminBirthdate),
            type: 'COMPANY_ADMIN',
            company_id: Number(createdCompany.companyId)
          })
        ),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: () => {
          this.successMessage = 'Compañía y administrador creados exitosamente.';
          this.form.reset();
        },
        error: (err) => {
          this.errorMessage = extractErrorMessage(err);
        }
      });
  }
}
