// Refleja SolicitudAutorizacionDescuentoDTO y AutorizacionDescuentoResponseDTO del
// backend (T6). Ver docs/requerimientos/ventas/AUTORIZACION_DESCUENTO_MANUAL.md,
// sección "Contrato para el frontend".

export interface SolicitudAutorizacionDescuento {
  username: string;
  password: string;
  productoId: number;
  cantidad: number;
  porcentaje: number;
  motivo: string;
  carritoId: string;
}

/**
 * `referencia` es el token en claro: se entrega una única vez, el backend solo
 * guarda su hash. Los campos de comparación contra la promoción automática son
 * informativos, no se aplican solos — hace falta enviar `referencia` en la venta.
 */
export interface AutorizacionDescuentoResponse {
  referencia: string;
  expiraEn: string;
  porcentaje: number;
  montoDescuentoEstimado: number;
  precioFinalEstimado: number;
  promocionAutomaticaDisponible: boolean;
  montoPromocionAutomatica: number;
  diferenciaVsPromocionAutomatica: number;
}
