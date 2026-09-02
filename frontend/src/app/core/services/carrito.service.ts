import { Injectable, computed, signal } from '@angular/core';
import { AutorizacionManualLinea, LineaCarrito } from '../models/carrito.model';
import { ClienteResumen } from '../models/cliente.model';
import { ProductoListado } from '../models/producto.model';

/**
 * Estado reactivo del carrito de venta en curso. Es exclusivamente de presentación:
 * agrega, quita y modifica cantidades en memoria, y deriva un subtotal estimado. No
 * decide precios, promociones ni el total definitivo — eso lo calcula el backend al
 * confirmar el cobro (Sub-tarea 4). `clienteSeleccionado` en `null` significa cliente
 * de mostrador, misma semántica que `clienteId: null` en `POST /api/v1/ventas`.
 */
@Injectable({
  providedIn: 'root',
})
export class CarritoService {
  private readonly _lineas = signal<LineaCarrito[]>([]);
  private readonly _clienteSeleccionado = signal<ClienteResumen | null>(null);
  // Identifica esta operación de venta ante el backend para atar autorizaciones de
  // descuento manual (T6) a este carrito y no a otro. Se genera perezosamente y se
  // renueva en cada vaciar() (nueva venta = nuevo carrito).
  private readonly _carritoId = signal(crypto.randomUUID());

  readonly lineas = this._lineas.asReadonly();
  readonly clienteSeleccionado = this._clienteSeleccionado.asReadonly();
  readonly carritoId = this._carritoId.asReadonly();

  readonly totalArticulos = computed(() => this._lineas().reduce((acc, l) => acc + l.cantidad, 0));

  /** Suma precio de lista × cantidad de todas las líneas. Estimado, no definitivo. */
  readonly subtotalEstimado = computed(() =>
    this._lineas().reduce((acc, l) => acc + l.precioVenta * l.cantidad, 0),
  );

  agregarProducto(producto: ProductoListado): void {
    const existente = this._lineas().find((l) => l.productoId === producto.id);
    if (existente) {
      this.establecerCantidad(producto.id, existente.cantidad + 1);
      return;
    }
    this._lineas.update((actuales) => [
      ...actuales,
      {
        productoId: producto.id,
        nombre: producto.nombre,
        codigoBarras: producto.codigoBarras,
        precioVenta: producto.precioVenta,
        cantidad: 1,
        cantidadDesconocida: producto.cantidadDesconocida,
        stockActual: producto.stockActual,
        autorizacionManual: null,
      },
    ]);
  }

  incrementar(productoId: number): void {
    const linea = this._lineas().find((l) => l.productoId === productoId);
    if (linea) {
      this.establecerCantidad(productoId, linea.cantidad + 1);
    }
  }

  /** Decrementar hasta 1 reduce la cantidad; desde 1, quita la línea. */
  decrementar(productoId: number): void {
    const linea = this._lineas().find((l) => l.productoId === productoId);
    if (!linea) {
      return;
    }
    if (linea.cantidad <= 1) {
      this.eliminar(productoId);
      return;
    }
    this.establecerCantidad(productoId, linea.cantidad - 1);
  }

  /**
   * Fija una cantidad exacta (p. ej. desde un input numérico). Se limita a 1 como
   * mínimo — para llegar a 0 se usa `eliminar()` explícitamente — y al stock conocido
   * como máximo, cuando el producto no tiene cantidad desconocida. Es una ayuda de UX,
   * no el control real: el backend siempre revalida el stock al confirmar la venta.
   *
   * Cualquier autorización de descuento manual (T6) sobre la línea se descarta aquí:
   * fue emitida para una cantidad exacta, y cambiarla la invalida.
   */
  establecerCantidad(productoId: number, cantidad: number): void {
    this._lineas.update((actuales) =>
      actuales.map((linea) => {
        if (linea.productoId !== productoId) {
          return linea;
        }
        const maximo = linea.cantidadDesconocida ? Number.MAX_SAFE_INTEGER : Math.max(linea.stockActual, 1);
        const cantidadEntera = Math.trunc(cantidad) || 1;
        const nuevaCantidad = Math.min(Math.max(1, cantidadEntera), maximo);
        return { ...linea, cantidad: nuevaCantidad, autorizacionManual: null };
      }),
    );
  }

  eliminar(productoId: number): void {
    this._lineas.update((actuales) => actuales.filter((l) => l.productoId !== productoId));
  }

  seleccionarCliente(cliente: ClienteResumen | null): void {
    this._clienteSeleccionado.set(cliente);
  }

  /** Aplica una autorización de descuento manual (T6) ya emitida a una línea existente. */
  otorgarDescuentoManual(productoId: number, autorizacion: AutorizacionManualLinea): void {
    this._lineas.update((actuales) =>
      actuales.map((linea) => (linea.productoId === productoId ? { ...linea, autorizacionManual: autorizacion } : linea)),
    );
  }

  /**
   * Quita el descuento manual de una línea sin tocar su cantidad. La autorización ya
   * emitida por el backend no se cancela (no existe endpoint para eso): simplemente
   * deja de enviarse en la venta y expira sola a los 2 minutos si nadie la usa.
   */
  quitarDescuentoManual(productoId: number): void {
    this._lineas.update((actuales) =>
      actuales.map((linea) => (linea.productoId === productoId ? { ...linea, autorizacionManual: null } : linea)),
    );
  }

  /** Limpia el carrito completo tras confirmar una venta (o para empezar de nuevo). */
  vaciar(): void {
    this._lineas.set([]);
    this._clienteSeleccionado.set(null);
    this._carritoId.set(crypto.randomUUID());
  }
}
