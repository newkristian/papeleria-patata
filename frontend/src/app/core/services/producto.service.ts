import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Pagina,
  ProductoActualizarRequest,
  ProductoCrearRequest,
  ProductoDetalle,
  ProductoFiltros,
  ProductoListado,
} from '../models/producto.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/productos`;

  /** GET /api/v1/productos/buscar — búsqueda paginada básica por término. */
  buscar(termino: string, page = 0, size = 20): Observable<Pagina<ProductoListado>> {
    return this.buscarAvanzado({ termino }, page, size);
  }

  /** GET /api/v1/productos/buscar — búsqueda avanzada con múltiples filtros. */
  buscarAvanzado(filtros: ProductoFiltros = {}, page = 0, size = 20): Observable<Pagina<ProductoListado>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (filtros.termino && filtros.termino.trim() !== '') {
      params = params.set('termino', filtros.termino.trim());
    }
    if (filtros.categoriaId !== null && filtros.categoriaId !== undefined) {
      params = params.set('categoriaId', filtros.categoriaId.toString());
    }
    if (filtros.proveedorId !== null && filtros.proveedorId !== undefined) {
      params = params.set('proveedorId', filtros.proveedorId.toString());
    }
    if (filtros.activo !== null && filtros.activo !== undefined) {
      params = params.set('activo', filtros.activo.toString());
    }
    if (filtros.precioMin !== null && filtros.precioMin !== undefined) {
      params = params.set('precioMin', filtros.precioMin.toString());
    }
    if (filtros.precioMax !== null && filtros.precioMax !== undefined) {
      params = params.set('precioMax', filtros.precioMax.toString());
    }
    if (filtros.soloStockBajo !== null && filtros.soloStockBajo !== undefined) {
      params = params.set('soloStockBajo', filtros.soloStockBajo.toString());
    }

    return this.http.get<Pagina<ProductoListado>>(`${this.apiUrl}/buscar`, { params });
  }

  /** GET /api/v1/productos/codigo/{codigoBarras} — resultado único, 404 si no existe. */
  buscarPorCodigoBarras(codigoBarras: string): Observable<ProductoListado> {
    return this.http.get<ProductoListado>(`${this.apiUrl}/codigo/${encodeURIComponent(codigoBarras)}`);
  }

  /** GET /api/v1/productos/{id} — obtener detalle completo. */
  obtenerPorId(id: number): Observable<ProductoDetalle> {
    return this.http.get<ProductoDetalle>(`${this.apiUrl}/${id}`);
  }

  /** POST /api/v1/productos — crear producto. */
  crear(request: ProductoCrearRequest): Observable<ProductoDetalle> {
    return this.http.post<ProductoDetalle>(this.apiUrl, request);
  }

  /** PUT /api/v1/productos/{id} — actualizar producto existente. */
  actualizar(id: number, request: ProductoActualizarRequest): Observable<ProductoDetalle> {
    return this.http.put<ProductoDetalle>(`${this.apiUrl}/${id}`, request);
  }

  /** PATCH /api/v1/productos/{id}/desactivar — desactivar producto. */
  desactivar(id: number): Observable<ProductoDetalle> {
    return this.http.patch<ProductoDetalle>(`${this.apiUrl}/${id}/desactivar`, {});
  }

  /** PATCH /api/v1/productos/{id}/reactivar — reactivar producto. */
  reactivar(id: number): Observable<ProductoDetalle> {
    return this.http.patch<ProductoDetalle>(`${this.apiUrl}/${id}/reactivar`, {});
  }
}
