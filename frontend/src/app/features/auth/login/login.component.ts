import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  error: string | null = null;
  loading = false;

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.error = null;
      this.loading = true;
      this.authService.login(this.loginForm.getRawValue()).subscribe({
        next: () => {
          this.router.navigate(['/pos']);
        },
        error: (err) => {
          this.loading = false;
          this.error = 'Credenciales incorrectas o error en el servidor';
          console.error('Login error', err);
        }
      });
    }
  }
}
