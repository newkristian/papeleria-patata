import { Categoria } from './categoria.model';
import { Proveedor } from './proveedor.model';

// Modelos del catálogo de productos consumidos por el POS y el módulo administrativo.
// Reflejan los DTO reales del backend (ProductoListadoDTO, ProductoDetalleDTO, etc.).

/** Versión resumida de un producto, devuelta por GET /api/v1/productos/buscar. */
export interface ProductoListado {
  id: number;
  codigoBarras: string;
  nombre: string;
  categoriaNombre: string;
  proveedorNombre: string;
  precioVenta: number;
  stockActual: number;
  stockMinimo: number;
  activo: boolean;
  cantidadDesconocida: boolean;
  urlThumbnail: string | null;
  tieneFotos: boolean;
}

/** Detalle completo de un producto devuelto por GET /api/v1/productos/{id}. */
export interface ProductoDetalle {
  id: number;
  codigoBarras: string;
  nombre: string;
  descripcion: string | null;
  categoria: Categoria;
  proveedor: Proveedor;
  costoCompra: number;
  porcentajeGanancia: number;
  precioVenta: number;
  stockMinimo: number;
  stockActual: number;
  unidadMedida: string;
  activo: boolean;
  cantidadDesconocida: boolean;
  fechaCreacion: string;
  fechaActualizacion: string;
  fotos: any[];
  fotoPrincipal: any | null;
}

/** Parámetros para creación de producto (POST /api/v1/productos). */
export interface ProductoCrearRequest {
  codigoBarras: string;
  nombre: string;
  descripcion?: string | null;
  categoriaId: number;
  proveedorId?: number | null;
  costoCompra: number;
  stockMinimo?: number;
  unidadMedida: string;
  porcentajeGananciaManual?: number | null;
  cantidadDesconocida?: boolean;
}

/** Parámetros para actualización de producto (PUT /api/v1/productos/{id}). */
export interface ProductoActualizarRequest {
  codigoBarras: string;
  nombre: string;
  descripcion?: string | null;
  categoriaId: number;
  proveedorId?: number | null;
  costoCompra: number;
  stockMinimo: number;
  unidadMedida: string;
  porcentajeGananciaManual?: number | null;
}

/** Filtros para búsqueda avanzada de productos. */
export interface ProductoFiltros {
  termino?: string | null;
  categoriaId?: number | null;
  proveedorId?: number | null;
  activo?: boolean | null;
  precioMin?: number | null;
  precioMax?: number | null;
  soloStockBajo?: boolean | null;
}

/**
 * Página tal como la serializa Spring con
 * `spring.data.web.pageable.serialization-mode: via-dto` — nunca el `PageImpl` crudo
 * de Spring Data. Contrato verificado contra el backend real, no asumido.
 */
export interface Pagina<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}
