import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  template: `
    <nav style="background:#3e2a1e;color:#fff;padding:12px 24px;display:flex;justify-content:space-between;align-items:center;">
      <div>
        <a routerLink="/menu" style="color:#fff;text-decoration:none;font-weight:bold;">☕ Cafe Management</a>
      </div>
      <div style="display:flex;gap:16px;align-items:center;">
        <ng-container *ngIf="auth.isLoggedIn(); else guestLinks">
          <span>{{ auth.currentUser()?.name }} ({{ auth.getRole() }})</span>
          <a *ngIf="auth.getRole()==='WORKER' || auth.getRole()==='ADMIN'" routerLink="/worker/queue" style="color:#fff;">Queue</a>
          <a *ngIf="auth.getRole()==='ADMIN'" routerLink="/admin/menu" style="color:#fff;">Menu editor</a>
          <a *ngIf="auth.getRole()==='ADMIN'" routerLink="/admin/staff" style="color:#fff;">Staff</a>
          <a *ngIf="auth.getRole()==='USER'" routerLink="/my-orders" style="color:#fff;">My orders</a>
          <button (click)="logout()">Logout</button>
        </ng-container>
        <ng-template #guestLinks>
          <a routerLink="/login" style="color:#fff;">Login</a>
          <a routerLink="/register" style="color:#fff;">Register</a>
        </ng-template>
      </div>
    </nav>
    <router-outlet></router-outlet>
  `
})
export class AppComponent {
  constructor(public auth: AuthService) {}

  logout(): void {
    this.auth.logout();
    window.location.href = '/login';
  }
}
