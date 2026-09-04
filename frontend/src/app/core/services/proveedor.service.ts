import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../models/producto.model';
import { Proveedor, ProveedorRequest } from '../models/proveedor.model';

@Injectable({
  providedIn: 'root',
})
export class ProveedorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/proveedores`;

  listarTodos(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.apiUrl);
  }

  listarActivos(): Observable<Proveedor[]> {
    return this.listarTodos();
  }

  buscar(termino?: string | null, activo?: boolean | null, page = 0, size = 20): Observable<Pagina<Proveedor>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (termino && termino.trim() !== '') {
      params = params.set('termino', termino.trim());
    }

    if (activo !== null && activo !== undefined) {
      params = params.set('activo', activo.toString());
    }

    return this.http.get<Pagina<Proveedor>>(`${this.apiUrl}/buscar`, { params });
  }

  obtenerPorId(id: number): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.apiUrl}/${id}`);
  }

  contarProductosAsignados(id: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${id}/productos/conteo`);
  }

  crear(proveedor: ProveedorRequest): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.apiUrl, proveedor);
  }

  actualizar(id: number, proveedor: ProveedorRequest): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/${id}`, proveedor);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
