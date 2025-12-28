import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { extractErrorMessage } from '../../core/error.util';

@Component({
  standalone: true,
  selector: 'app-global-commission',
  templateUrl: './global-commission.component.html',
  styleUrls: ['./global-commission.component.css'],
  imports: [CommonModule, ReactiveFormsModule]
})
export class GlobalCommissionComponent implements OnInit {
  form: FormGroup;
  loading = false;
  saving = false;
  successMessage = '';
  errorMessage = '';
  currentCommission: number | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.form = this.fb.group({
      commission: [null, [Validators.required, Validators.min(0), Validators.max(100)]]
    });
  }

  ngOnInit(): void {
    this.loadCurrentCommission();
  }

  get f() {
    return this.form.controls;
  }

  loadCurrentCommission(): void {
    this.loading = true;
    this.auth
      .getGlobalCommission()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (gc) => {
          const value = Number(gc?.commission);
          this.currentCommission = Number.isFinite(value) ? value : null;
          if (Number.isFinite(value)) {
            this.form.patchValue({ commission: value });
          }
        },
        error: () => {
          this.currentCommission = null;
        }
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    const commissionValue = Number(this.form.value.commission);

    this.auth
      .updateGlobalCommission(commissionValue)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Comisión global actualizada correctamente.';
          this.currentCommission = commissionValue;
        },
        error: (err) => {
          this.errorMessage = extractErrorMessage(err);
        }
      });
  }
}
