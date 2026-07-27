import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { MenuService } from '../../services/menu.service';
import { Order, OrderStatus } from '../../models/order.model';
import { MenuItem } from '../../models/menu-item.model';

@Component({
  selector: 'app-worker-queue',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h2>Order Queue</h2>
      <p *ngIf="error" class="error">{{ error }}</p>
      <button (click)="load()" style="margin-bottom:12px;">Refresh</button>

      <div *ngFor="let order of orders" class="card">
        <div style="display:flex;justify-content:space-between;">
          <strong>Order #{{ order.id }} — {{ order.customerName }}</strong>
          <span class="badge">{{ order.status }}</span>
        </div>
        <div *ngFor="let item of order.items">{{ item.menuItemName }} x {{ item.quantity }}</div>
        <p *ngIf="order.handledByName">Handled by: {{ order.handledByName }}</p>

        <button *ngIf="order.status === 'PLACED'" (click)="accept(order.id)">Accept</button>
        <button *ngIf="order.status === 'ACCEPTED'" (click)="setStatus(order.id, 'PREPARING')">Start preparing</button>
        <button *ngIf="order.status === 'PREPARING'" (click)="setStatus(order.id, 'READY')">Mark ready</button>
        <button *ngIf="order.status === 'READY'" (click)="setStatus(order.id, 'COMPLETED')">Mark completed</button>
      </div>

      <hr style="margin:32px 0;">

      <h2>Stock Management</h2>
      <div *ngFor="let item of menuItems" class="card" style="display:flex;justify-content:space-between;align-items:center;">
        <div>{{ item.name }} — currently {{ item.stockQuantity }} in stock</div>
        <div>
          <input type="number" min="0" [(ngModel)]="stockEdits[item.id]" [ngModelOptions]="{standalone: true}"
                 style="width:80px;display:inline-block;margin-right:8px;" [placeholder]="item.stockQuantity.toString()">
          <button (click)="updateStock(item)">Update</button>
        </div>
      </div>
    </div>
  `
})
export class WorkerQueueComponent implements OnInit {
  orders: Order[] = [];
  menuItems: MenuItem[] = [];
  stockEdits: Record<number, number> = {};
  error = '';

  constructor(private orderService: OrderService, private menuService: MenuService) {}

  ngOnInit(): void {
    this.load();
    this.loadMenu();
  }

  load(): void {
    this.orderService.getQueue().subscribe({
      next: (data) => (this.orders = data),
      error: () => (this.error = 'Could not load order queue.')
    });
  }

  loadMenu(): void {
    this.menuService.getStaffMenu().subscribe({ next: (data) => (this.menuItems = data) });
  }

  accept(id: number): void {
    this.orderService.acceptOrder(id).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err.error?.message || 'Could not accept order — someone may have already claimed it.')
    });
  }

  setStatus(id: number, status: OrderStatus): void {
    this.orderService.updateStatus(id, status).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err.error?.message || 'Could not update status')
    });
  }

  updateStock(item: MenuItem): void {
    const qty = this.stockEdits[item.id];
    if (qty === undefined || qty === null) return;
    this.menuService.updateStock(item.id, qty).subscribe({
      next: () => this.loadMenu(),
      error: () => (this.error = 'Could not update stock')
    });
  }
}
