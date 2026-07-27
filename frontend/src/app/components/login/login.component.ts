import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container" style="max-width:400px;">
      <h2>Login</h2>
      <form (ngSubmit)="onSubmit()">
        <input type="email" placeholder="Email" [(ngModel)]="email" name="email" required>
        <input type="password" placeholder="Password" [(ngModel)]="password" name="password" required>
        <button type="submit" [disabled]="loading">{{ loading ? 'Logging in...' : 'Login' }}</button>
      </form>
      <p *ngIf="error" class="error">{{ error }}</p>
      <p>No account? <a routerLink="/register">Register</a></p>
      <p style="font-size:12px;color:#888;">Default admin: admin&#64;cafe.com / Admin&#64;123</p>
    </div>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        this.loading = false;
        this.redirectByRole(res.role);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Login failed';
      }
    });
  }

  private redirectByRole(role: string): void {
    if (role === 'ADMIN') this.router.navigate(['/admin/menu']);
    else if (role === 'WORKER') this.router.navigate(['/worker/queue']);
    else this.router.navigate(['/menu']);
  }
}
