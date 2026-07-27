import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MenuItem } from '../models/menu-item.model';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class MenuService {
  constructor(private http: HttpClient) {}

  getPublicMenu(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${BASE_URL}/menu`);
  }

  getStaffMenu(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${BASE_URL}/menu/staff-view`);
  }

  create(item: Partial<MenuItem>): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${BASE_URL}/menu`, item);
  }

  update(id: number, item: Partial<MenuItem>): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${BASE_URL}/menu/${id}`, item);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/menu/${id}`);
  }

  updateStock(id: number, quantity: number): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${BASE_URL}/worker/menu/${id}/stock`, { quantity });
  }
}
