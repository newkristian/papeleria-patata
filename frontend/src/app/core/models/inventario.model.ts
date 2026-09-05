// Modelos para la gestión de inventario, movimientos y ajustes de stock.

export type TipoMovimiento = 'ENTRADA' | 'SALIDA' | 'AJUSTE';

export interface InventarioMovimiento {
  id: number;
  productoId: number;
  productoNombre: string;
  productoCodigoBarras: string;
  usuarioId: number;
  usuarioNombre: string;
  tipo: TipoMovimiento;
  cantidad: number;
  motivo: string;
  costoUnitario: number | null;
  fechaMovimiento: string;
}

export interface InventarioMovimientoRequest {
  productoId: number;
  cantidad: number;
  motivo: string;
  costoUnitario: number;
}

export interface AjusteInventarioRequest {
  productoId: number;
  cantidad: number;
  motivo: string;
  nuevoCostoCompra?: number | null;
  esFijarStockAbsoluto?: boolean;
}

export interface FiltrosMovimientos {
  productoId?: number | null;
  tipo?: TipoMovimiento | null;
  usuarioId?: number | null;
  fechaInicio?: string | null;
  fechaFin?: string | null;
}
