import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container" style="max-width:400px;">
      <h2>Create account</h2>
      <p style="font-size:13px;color:#888;">Public signup always creates a USER account. Worker/Admin accounts are created by an admin.</p>
      <form (ngSubmit)="onSubmit()">
        <input placeholder="Full name" [(ngModel)]="name" name="name" required>
        <input type="email" placeholder="Email" [(ngModel)]="email" name="email" required>
        <input type="password" placeholder="Password (min 6 chars)" [(ngModel)]="password" name="password" required>
        <button type="submit" [disabled]="loading">{{ loading ? 'Creating...' : 'Register' }}</button>
      </form>
      <p *ngIf="error" class="error">{{ error }}</p>
      <p>Already have an account? <a routerLink="/login">Login</a></p>
    </div>
  `
})
export class RegisterComponent {
  name = '';
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    this.authService.register(this.name, this.email, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/menu']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Registration failed';
      }
    });
  }
}
