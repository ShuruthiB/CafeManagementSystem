import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderStatus } from '../models/order.model';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl;

export interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}

  // USER
  placeOrder(items: OrderItemRequest[]): Observable<Order> {
    return this.http.post<Order>(`${BASE_URL}/orders`, { items });
  }

  myOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${BASE_URL}/orders/my`);
  }

  cancelOrder(id: number): Observable<Order> {
    return this.http.put<Order>(`${BASE_URL}/orders/${id}/cancel`, {});
  }

  // WORKER / ADMIN
  getQueue(): Observable<Order[]> {
    return this.http.get<Order[]>(`${BASE_URL}/worker/orders/queue`);
  }

  acceptOrder(id: number): Observable<Order> {
    return this.http.put<Order>(`${BASE_URL}/worker/orders/${id}/accept`, {});
  }

  updateStatus(id: number, status: OrderStatus): Observable<Order> {
    return this.http.put<Order>(`${BASE_URL}/worker/orders/${id}/status`, { status });
  }

  // ADMIN
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${BASE_URL}/admin/orders`);
  }
}
