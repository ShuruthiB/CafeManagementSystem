import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { MenuComponent } from './components/menu/menu.component';
import { MyOrdersComponent } from './components/place-order/place-order.component';
import { WorkerQueueComponent } from './components/worker-queue/worker-queue.component';
import { AdminMenuComponent } from './components/admin-menu/admin-menu.component';
import { AdminStaffComponent } from './components/admin-staff/admin-staff.component';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'menu', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },

  // public / all-roles
  { path: 'menu', component: MenuComponent },

  // USER only
  { path: 'my-orders', component: MyOrdersComponent, canActivate: [authGuard, roleGuard(['USER'])] },

  // WORKER + ADMIN
  { path: 'worker/queue', component: WorkerQueueComponent, canActivate: [authGuard, roleGuard(['WORKER', 'ADMIN'])] },

  // ADMIN only
  { path: 'admin/menu', component: AdminMenuComponent, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin/staff', component: AdminStaffComponent, canActivate: [authGuard, roleGuard(['ADMIN'])] },

  { path: '**', redirectTo: 'menu' }
];
