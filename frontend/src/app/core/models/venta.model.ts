// Refleja los DTO reales de venta del backend (VentaRequestDTO, VentaResponseDTO,
// DetalleVentaResponseDTO, ProductoResponseDTO — este último embebido en el detalle
// usa categoria/proveedor como String con solo el nombre, a diferencia de
// ProductoDetalleDTO que sí los anida como objeto).

export type MetodoPago = 'EFECTIVO' | 'TARJETA_CREDITO' | 'TARJETA_DEBITO' | 'TRANSFERENCIA';
export type TipoDescuento = 'NINGUNO' | 'CANTIDAD' | 'CLIENTE' | 'MANUAL';
export type EstadoVenta = 'COMPLETADA' | 'CANCELADA';

export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number;
  /** Referencia opaca de autorización de descuento manual (T6). Opcional. */
  autorizacionDescuento?: string;
}

export interface VentaRequest {
  clienteId: number | null;
  metodoPago: MetodoPago;
  detalles: DetalleVentaRequest[];
  /** Obligatorio solo si algún detalle trae autorizacionDescuento; siempre seguro de enviar. */
  carritoId?: string;
}

interface ProductoVentaResumen {
  id: number;
  codigoBarras: string;
  nombre: string;
  descripcion: string | null;
  categoria: string;
  proveedor: string;
  precioVenta: number;
  stockActual: number;
}

export interface DetalleVentaResponse {
  id: number;
  producto: ProductoVentaResumen;
  cantidad: number;
  precioListaUnitario: number;
  tipoDescuento: TipoDescuento;
  porcentajeDescuento: number;
  montoDescuento: number;
  promocionProductoId: number | null;
  promocionClienteId: number | null;
  precioUnitarioFinal: number;
  subtotal: number;
  autorizadoPorUsuarioId: number | null;
  motivoDescuento: string | null;
  autorizacionDescuentoId: number | null;
}

interface UsuarioVentaResumen {
  id: number;
  username: string;
  nombre: string;
  apellidos: string | null;
  email: string;
  rol: string;
  tiendaId: number | null;
  tiendaNombre: string | null;
  activo: boolean;
  requiereCambioPassword: boolean;
}

interface TiendaVentaResumen {
  id: number;
  nombre: string;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
}

interface ClienteVentaResumen {
  id: number;
  nombre: string;
  telefono: string;
  totalCompras: number;
  nivel: string;
}

export interface VentaResponse {
  id: number;
  folio: string;
  usuario: UsuarioVentaResumen;
  tienda: TiendaVentaResumen;
  cliente: ClienteVentaResumen;
  ventaAnonima: boolean;
  fechaVenta: string;
  subtotal: number;
  descuento: number;
  total: number;
  metodoPago: MetodoPago;
  estado: EstadoVenta;
  detalles: DetalleVentaResponse[];
}
