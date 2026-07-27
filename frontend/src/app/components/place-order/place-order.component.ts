import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order.model';

// Despite the folder name, this is the USER's "My Orders" history/cancel screen.
@Component({
  selector: 'app-my-orders',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <h2>My Orders</h2>
      <p *ngIf="error" class="error">{{ error }}</p>
      <p *ngIf="orders.length === 0 && !error">No orders yet.</p>

      <div *ngFor="let order of orders" class="card">
        <div style="display:flex;justify-content:space-between;">
          <strong>Order #{{ order.id }}</strong>
          <span class="badge">{{ order.status }}</span>
        </div>
        <p style="margin:4px 0;color:#666;">{{ order.createdAt | date:'medium' }}</p>
        <div *ngFor="let item of order.items">{{ item.menuItemName }} x {{ item.quantity }} — ₹{{ item.priceAtOrderTime }}</div>
        <p><strong>Total: ₹{{ order.totalAmount }}</strong></p>
        <button *ngIf="order.status === 'PLACED' || order.status === 'ACCEPTED'"
                (click)="cancel(order.id)" style="background:#b3261e;">
          Cancel order
        </button>
      </div>
    </div>
  `
})
export class MyOrdersComponent implements OnInit {
  orders: Order[] = [];
  error = '';

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.orderService.myOrders().subscribe({
      next: (data) => (this.orders = data),
      error: () => (this.error = 'Could not load your orders.')
    });
  }

  cancel(id: number): void {
    this.orderService.cancelOrder(id).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err.error?.message || 'Could not cancel order')
    });
  }
}
