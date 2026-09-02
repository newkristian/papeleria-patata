import { Component, inject } from '@angular/core';
import { ProductoListado } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { BuscadorProductosComponent } from '../buscador-productos/buscador-productos';
import { CarritoComponent } from '../carrito/carrito';

@Component({
  selector: 'app-pos-layout',
  imports: [BuscadorProductosComponent, CarritoComponent],
  templateUrl: './pos-layout.html',
})
export class PosLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly carritoService = inject(CarritoService);

  cerrarSesion(): void {
    this.authService.logout();
  }

  onProductoSeleccionado(producto: ProductoListado): void {
    this.carritoService.agregarProducto(producto);
  }
}
