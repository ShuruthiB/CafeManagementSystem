import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface StaffUser {
  id: number;
  name: string;
  email: string;
  role: string;
  enabled: boolean;
}

@Component({
  selector: 'app-admin-staff',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h2>Admin — Staff & Users</h2>
      <p *ngIf="error" class="error">{{ error }}</p>
      <p *ngIf="success" style="color:green;">{{ success }}</p>

      <div class="card">
        <h3>Create WORKER / ADMIN account</h3>
        <input placeholder="Name" [(ngModel)]="form.name" name="name">
        <input placeholder="Email" [(ngModel)]="form.email" name="email">
        <input placeholder="Password" [(ngModel)]="form.password" name="password">
        <select [(ngModel)]="form.role" name="role">
          <option value="WORKER">Worker</option>
          <option value="ADMIN">Admin</option>
        </select>
        <button (click)="createStaff()">Create</button>
      </div>

      <div *ngFor="let user of users" class="card" style="display:flex;justify-content:space-between;align-items:center;">
        <div>
          <strong>{{ user.name }}</strong> <span class="badge">{{ user.role }}</span>
          <p style="margin:4px 0;color:#666;">{{ user.email }} &middot; {{ user.enabled ? 'Active' : 'Disabled' }}</p>
        </div>
        <button (click)="toggle(user)" [style.background]="user.enabled ? '#b3261e' : '#2e7d32'">
          {{ user.enabled ? 'Disable' : 'Enable' }}
        </button>
      </div>
    </div>
  `
})
export class AdminStaffComponent implements OnInit {
  users: StaffUser[] = [];
  error = '';
  success = '';
  form = { name: '', email: '', password: '', role: 'WORKER' };
  private baseUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.http.get<StaffUser[]>(`${this.baseUrl}/users`).subscribe({
      next: (data) => (this.users = data),
      error: () => (this.error = 'Could not load users')
    });
  }

  createStaff(): void {
    this.error = '';
    this.success = '';
    this.http.post(`${this.baseUrl}/staff`, this.form).subscribe({
      next: () => {
        this.success = 'Staff account created';
        this.form = { name: '', email: '', password: '', role: 'WORKER' };
        this.load();
      },
      error: (err) => (this.error = err.error?.message || 'Could not create staff account')
    });
  }

  toggle(user: StaffUser): void {
    const action = user.enabled ? 'disable' : 'enable';
    this.http.put(`${this.baseUrl}/users/${user.id}/${action}`, {}).subscribe({
      next: () => this.load(),
      error: () => (this.error = 'Could not update user status')
    });
  }
}
