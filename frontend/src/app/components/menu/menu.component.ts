import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MenuService } from '../../services/menu.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { MenuItem } from '../../models/menu-item.model';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <h2>Menu</h2>
      <p *ngIf="error" class="error">{{ error }}</p>

      <div *ngFor="let item of items" class="card" style="display:flex;justify-content:space-between;align-items:center;">
        <div>
          <strong>{{ item.name }}</strong> <span class="badge">{{ item.category }}</span>
          <p style="margin:4px 0;color:#666;">{{ item.description }}</p>
          <p style="margin:0;">₹{{ item.price }} &middot; {{ item.stockQuantity }} left</p>
        </div>
        <div *ngIf="isUser()">
          <input type="number" min="1" [max]="item.stockQuantity" [(ngModel)]="quantities[item.id]"
                 name="qty-{{item.id}}" style="width:60px;display:inline-block;margin-right:8px;">
          <button (click)="addToCart(item)">Add</button>
        </div>
      </div>

      <div *ngIf="isUser() && cart.length > 0" class="card" style="background:#fff8ee;">
        <h3>Your cart</h3>
        <div *ngFor="let line of cart">{{ line.name }} x {{ line.quantity }}</div>
        <button (click)="placeOrder()" [disabled]="placing">{{ placing ? 'Placing...' : 'Place order' }}</button>
        <button (click)="clearCart()" style="background:#999;margin-left:8px;">Clear</button>
        <p *ngIf="orderError" class="error">{{ orderError }}</p>
        <p *ngIf="orderSuccess" style="color:green;">{{ orderSuccess }}</p>
      </div>

      <p *ngIf="isUser()"><a routerLink="/my-orders">View my orders →</a></p>
    </div>
  `
})
export class MenuComponent implements OnInit {
  items: MenuItem[] = [];
  error = '';
  quantities: Record<number, number> = {};
  cart: { id: number; name: string; quantity: number }[] = [];
  placing = false;
  orderError = '';
  orderSuccess = '';

  constructor(
    private menuService: MenuService,
    private orderService: OrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.menuService.getPublicMenu().subscribe({
      next: (data) => (this.items = data),
      error: () => (this.error = 'Could not load menu. Is the backend running?')
    });
  }

  isUser(): boolean {
    return this.authService.getRole() === 'USER';
  }

  addToCart(item: MenuItem): void {
    const qty = this.quantities[item.id] || 1;
    const existing = this.cart.find(c => c.id === item.id);
    if (existing) existing.quantity = qty;
    else this.cart.push({ id: item.id, name: item.name, quantity: qty });
  }

  clearCart(): void {
    this.cart = [];
    this.orderSuccess = '';
    this.orderError = '';
  }

  placeOrder(): void {
    this.placing = true;
    this.orderError = '';
    const items = this.cart.map(c => ({ menuItemId: c.id, quantity: c.quantity }));
    this.orderService.placeOrder(items).subscribe({
      next: () => {
        this.placing = false;
        this.orderSuccess = 'Order placed!';
        this.cart = [];
      },
      error: (err) => {
        this.placing = false;
        this.orderError = err.error?.message || 'Could not place order';
      }
    });
  }
}
