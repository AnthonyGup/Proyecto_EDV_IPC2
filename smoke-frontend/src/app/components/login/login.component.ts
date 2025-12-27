import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { extractErrorMessage } from '../../core/error.util';
import { SessionService } from '../../core/session.service';
import { User } from '../../models';

@Component({
  standalone: true,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  imports: [ReactiveFormsModule, RouterLink, NgIf]
})
export class LoginComponent implements OnInit {
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

  ngOnInit(): void {
    const user = this.session.getUser();
    if (user) {
      if (user.type === 'SYSTEM_ADMIN') {
        this.router.navigate(['/admin']);
      } else if (user.type === 'COMPANY_ADMIN') {
        this.router.navigate(['/company/admin']);
      }
    }
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.errorMessage = null;
      this.isLoading = true;
      const { email, password } = this.loginForm.value;
      
      this.authService.login(email, password).subscribe({
        next: (user: User) => {
          this.session.saveUser(user);
          
          if (user.type === 'SYSTEM_ADMIN') {
            this.router.navigate(['/admin']);
          } else if (user.type === 'COMPANY_ADMIN') {
            this.router.navigate(['/company/admin']);
          } else if (user.type === 'GAMER') {
            this.errorMessage = 'Los usuarios tipo GAMER no tienen acceso al panel de administración.';
            this.session.clear();
          } else {
            this.errorMessage = 'Tipo de usuario no reconocido.';
            this.session.clear();
          }
          this.isLoading = false;
        },
        error: (error) => {
          this.errorMessage = extractErrorMessage(error);
          this.isLoading = false;
        }
      });
    }
  }
}