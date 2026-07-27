import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container">
      <h2>Access denied</h2>
      <p>You don't have permission to view this page.</p>
      <a routerLink="/menu">Go back to menu</a>
    </div>
  `
})
export class UnauthorizedComponent {}
