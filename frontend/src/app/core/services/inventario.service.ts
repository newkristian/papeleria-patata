import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AjusteInventarioRequest,
  FiltrosMovimientos,
  InventarioMovimiento,
  InventarioMovimientoRequest,
} from '../models/inventario.model';
import { Pagina, ProductoDetalle } from '../models/producto.model';

@Injectable({
  providedIn: 'root',
})
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly inventarioUrl = `${environment.apiUrl}/api/v1/inventario`;
  private readonly productosUrl = `${environment.apiUrl}/api/v1/productos`;

  /** POST /api/v1/inventario/entradas — Registrar entrada manual */
  registrarEntrada(request: InventarioMovimientoRequest): Observable<InventarioMovimiento> {
    return this.http.post<InventarioMovimiento>(`${this.inventarioUrl}/entradas`, request);
  }

  /** POST /api/v1/inventario/salidas — Registrar salida manual */
  registrarSalida(request: InventarioMovimientoRequest): Observable<InventarioMovimiento> {
    return this.http.post<InventarioMovimiento>(`${this.inventarioUrl}/salidas`, request);
  }

  /** POST /api/v1/productos/ajustar-inventario — Ajuste absoluto o relativo */
  ajustarInventario(request: AjusteInventarioRequest): Observable<ProductoDetalle> {
    return this.http.post<ProductoDetalle>(`${this.productosUrl}/ajustar-inventario`, request);
  }

  /** GET /api/v1/inventario/movimientos — Historial paginado con filtros */
  obtenerMovimientos(
    filtros: FiltrosMovimientos = {},
    page = 0,
    size = 20
  ): Observable<Pagina<InventarioMovimiento>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (filtros.productoId !== null && filtros.productoId !== undefined) {
      params = params.set('productoId', filtros.productoId.toString());
    }
    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }
    if (filtros.usuarioId !== null && filtros.usuarioId !== undefined) {
      params = params.set('usuarioId', filtros.usuarioId.toString());
    }
    if (filtros.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }
    if (filtros.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }

    return this.http.get<Pagina<InventarioMovimiento>>(`${this.inventarioUrl}/movimientos`, { params });
  }

  /** Alias para obtenerMovimientos */
  listarMovimientos(
    filtros: FiltrosMovimientos = {},
    page = 0,
    size = 20
  ): Observable<Pagina<InventarioMovimiento>> {
    return this.obtenerMovimientos(filtros, page, size);
  }
}
