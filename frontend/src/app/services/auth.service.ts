import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse, UserRole } from '../models/user.model';
import { environment } from '../../environments/environment';

const BASE_URL = `${environment.apiUrl}/auth`;
const STORAGE_KEY = 'cafe_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // signal so components can reactively show/hide UI based on login state
  currentUser = signal<LoginResponse | null>(this.loadFromStorage());

  constructor(private http: HttpClient) {}

  register(name: string, email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${BASE_URL}/register`, { name, email, password })
      .pipe(tap(res => this.persist(res)));
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${BASE_URL}/login`, { email, password })
      .pipe(tap(res => this.persist(res)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return this.currentUser()?.token ?? null;
  }

  getRole(): UserRole | null {
    return this.currentUser()?.role ?? null;
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  private persist(res: LoginResponse): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(res));
    this.currentUser.set(res);
  }

  private loadFromStorage(): LoginResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
