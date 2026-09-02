import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { ProductoListado } from '../../../core/models/producto.model';
import { BuscadorProductosComponent } from '../buscador-productos/buscador-productos';

@Component({
  selector: 'app-pos-layout',
  imports: [BuscadorProductosComponent],
  templateUrl: './pos-layout.html',
})
export class PosLayoutComponent {
  private readonly authService = inject(AuthService);

  // Lista temporal de solo lectura, únicamente para verificar visualmente que la
  // selección del buscador (Sub-tarea 2) funciona de punta a punta. Sin cantidades,
  // sin edición, sin totales: no es el carrito. La Sub-tarea 3 (CARRITO_VENTA.md) la
  // reemplaza por completo con estado reactivo real.
  readonly seleccionRecienteStub = signal<ProductoListado[]>([]);

  cerrarSesion(): void {
    this.authService.logout();
  }

  onProductoSeleccionado(producto: ProductoListado): void {
    this.seleccionRecienteStub.update((actuales) => [producto, ...actuales]);
  }
}
