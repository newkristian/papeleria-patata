import { Component, inject } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-pos-layout',
  imports: [],
  templateUrl: './pos-layout.html',
})
export class PosLayoutComponent {
  private readonly authService = inject(AuthService);

  cerrarSesion(): void {
    this.authService.logout();
  }
}
