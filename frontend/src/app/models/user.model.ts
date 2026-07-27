export type UserRole = 'USER' | 'WORKER' | 'ADMIN';

export interface LoginResponse {
  token: string;
  name: string;
  email: string;
  role: UserRole;
}
