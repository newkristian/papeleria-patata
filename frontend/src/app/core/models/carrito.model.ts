/**
 * Línea del carrito en el POS. `precioVenta` es el precio de lista al momento de
 * agregar el producto — una estimación para mostrar en pantalla, nunca la fuente de
 * verdad: el backend vuelve a leer el precio y recalcula todo (incluidas promociones
 * y descuentos) al confirmar la venta.
 */
export interface LineaCarrito {
  productoId: number;
  nombre: string;
  codigoBarras: string;
  precioVenta: number;
  cantidad: number;
  cantidadDesconocida: boolean;
  stockActual: number;
}
