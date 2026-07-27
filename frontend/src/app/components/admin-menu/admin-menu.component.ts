import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MenuService } from '../../services/menu.service';
import { MenuItem } from '../../models/menu-item.model';

@Component({
  selector: 'app-admin-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <h2>Admin — Menu Editor</h2>
      <p><a routerLink="/admin/staff">Manage staff →</a> &nbsp; <a routerLink="/worker/queue">View order queue →</a></p>
      <p *ngIf="error" class="error">{{ error }}</p>

      <div class="card">
        <h3>{{ editingId ? 'Edit item' : 'Add new item' }}</h3>
        <input placeholder="Name" [(ngModel)]="form.name" name="name">
        <input placeholder="Description" [(ngModel)]="form.description" name="description">
        <input placeholder="Category" [(ngModel)]="form.category" name="category">
        <input type="number" placeholder="Price" [(ngModel)]="form.price" name="price">
        <input type="number" placeholder="Stock quantity" [(ngModel)]="form.stockQuantity" name="stockQuantity">
        <button (click)="save()">{{ editingId ? 'Update' : 'Create' }}</button>
        <button *ngIf="editingId" (click)="resetForm()" style="background:#999;margin-left:8px;">Cancel edit</button>
      </div>

      <div *ngFor="let item of items" class="card" style="display:flex;justify-content:space-between;align-items:center;">
        <div>
          <strong>{{ item.name }}</strong> <span class="badge">{{ item.category }}</span>
          <p style="margin:4px 0;">₹{{ item.price }} &middot; stock: {{ item.stockQuantity }} &middot;
            {{ item.available ? 'Available' : 'Unavailable' }}</p>
        </div>
        <div>
          <button (click)="edit(item)">Edit</button>
          <button (click)="remove(item.id)" style="background:#b3261e;margin-left:8px;">Delete</button>
        </div>
      </div>
    </div>
  `
})
export class AdminMenuComponent implements OnInit {
  items: MenuItem[] = [];
  error = '';
  editingId: number | null = null;
  form = { name: '', description: '', category: '', price: 0, stockQuantity: 0 };

  constructor(private menuService: MenuService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.menuService.getStaffMenu().subscribe({
      next: (data) => (this.items = data),
      error: () => (this.error = 'Could not load menu')
    });
  }

  edit(item: MenuItem): void {
    this.editingId = item.id;
    this.form = { name: item.name, description: item.description, category: item.category,
                  price: item.price, stockQuantity: item.stockQuantity };
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { name: '', description: '', category: '', price: 0, stockQuantity: 0 };
  }

  save(): void {
    const action = this.editingId
      ? this.menuService.update(this.editingId, this.form)
      : this.menuService.create(this.form);

    action.subscribe({
      next: () => { this.resetForm(); this.load(); },
      error: (err) => (this.error = err.error?.message || 'Save failed')
    });
  }

  remove(id: number): void {
    this.menuService.delete(id).subscribe({
      next: () => this.load(),
      error: () => (this.error = 'Delete failed')
    });
  }
}
