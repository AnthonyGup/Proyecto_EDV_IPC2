import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { extractErrorMessage } from '../../core/error.util';
import { SessionService } from '../../core/session.service';

@Component({
  standalone: true,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  imports: [ReactiveFormsModule, RouterLink, NgIf]
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private session: SessionService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.errorMessage = null;
      this.isLoading = true;
      const { email, password } = this.loginForm.value;
      this.authService.login(email, password).subscribe(
        response => {
          console.log('Login successful', response);
          this.session.saveUser(response);
          if (response?.type === 'SYSTEM_ADMIN') {
            this.router.navigate(['/admin']);
          } else {
            this.errorMessage = 'No tienes permisos de administrador.';
            this.session.clear();
          }
          this.isLoading = false;
        },
        error => {
          console.error('Login failed', error);
          this.errorMessage = extractErrorMessage(error);
          this.isLoading = false;
        }
      );
    }
  }
}