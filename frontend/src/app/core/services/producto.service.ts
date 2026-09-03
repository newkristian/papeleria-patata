import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina, ProductoListado } from '../models/producto.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoService {
  private readonly apiUrl = `${environment.apiUrl}/api/v1/productos`;

  constructor(private http: HttpClient) {}

  /** GET /productos/buscar — búsqueda paginada por término (nombre, descripción o código). */
  buscar(termino: string, page = 0, size = 20): Observable<Pagina<ProductoListado>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (termino) {
      params = params.set('termino', termino);
    }
    return this.http.get<Pagina<ProductoListado>>(`${this.apiUrl}/buscar`, { params });
  }

  /** GET /productos/codigo/{codigoBarras} — resultado único, 404 si no existe. */
  buscarPorCodigoBarras(codigoBarras: string): Observable<ProductoListado> {
    return this.http.get<ProductoListado>(`${this.apiUrl}/codigo/${encodeURIComponent(codigoBarras)}`);
  }
}
