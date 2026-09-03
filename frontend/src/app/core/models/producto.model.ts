// Modelos del catálogo de productos consumidos por el POS. Reflejan los DTO reales
// del backend (ver ProductoListadoDTO y ProductoBusquedaDTO en
// backend/src/main/java/.../producto/), no se inventan campos adicionales.

/** Versión resumida de un producto, tal como la devuelve GET /productos/buscar. */
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
