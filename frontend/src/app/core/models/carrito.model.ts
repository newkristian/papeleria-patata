/**
 * Descuento manual autorizado (T6) aplicado a una línea del carrito. `referencia` es
 * la autorización opaca de un solo uso; se envía tal cual en `DetalleVentaRequest` al
 * confirmar la venta. Cualquier cambio de cantidad en la línea la invalida (ver
 * `CarritoService.establecerCantidad`).
 */
export interface AutorizacionManualLinea {
  referencia: string;
  porcentaje: number;
  expiraEn: string;
  motivo: string;
}

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
  autorizacionManual: AutorizacionManualLinea | null;
}
