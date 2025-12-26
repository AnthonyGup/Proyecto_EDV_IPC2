import { NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { extractErrorMessage } from '../../core/error.util';

@Component({
  standalone: true,
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  imports: [ReactiveFormsModule, RouterLink, NgIf]
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      mail: ['', [Validators.required, Validators.email]],
      nickname: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      birthdate: ['', Validators.required],
      country: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{7,15}$/)]],
      wallet: [0, [Validators.required, Validators.min(0)]]
    });
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.errorMessage = null;
      this.isLoading = true;

      const gamerData = this.registerForm.value;
      gamerData.wallet = parseFloat(gamerData.wallet) || 0;
      gamerData.phone = parseInt(gamerData.phone, 10);

      // Llama al servicio para registrar al gamer
      this.authService.registerGamer(gamerData).subscribe(
        // Si success: se registró correctamente
        response => {
          console.log('Registro exitoso', response);
          this.isLoading = false;
          // Después de 0.5s, redirige a login
          setTimeout(() => this.router.navigate(['/login']), 500);
        },
        // Si error: el servidor respondió con error
        error => {
          console.error('Registro fallido', error);
          // Extrae el mensaje de error y lo muestra
          this.errorMessage = extractErrorMessage(error);
          this.isLoading = false;
        }
      );
    }
  }
}
