import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProductoFoto } from '../models/producto-foto.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoFotoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/productos`;

  /** POST /api/v1/productos/{productoId}/fotos — Subir foto (asíncrono, responde 202 Accepted) */
  subirFoto(productoId: number, archivo: File, esPrincipal = false): Observable<ProductoFoto> {
    const formData = new FormData();
    formData.append('file', archivo);
    if (esPrincipal) {
      formData.append('esPrincipal', 'true');
    }
    return this.http.post<ProductoFoto>(`${this.apiUrl}/${productoId}/fotos`, formData);
  }

  /** GET /api/v1/productos/{productoId}/fotos — Listar todas las fotos */
  listarFotos(productoId: number): Observable<ProductoFoto[]> {
    return this.http.get<ProductoFoto[]>(`${this.apiUrl}/${productoId}/fotos`);
  }

  /** GET /api/v1/productos/{productoId}/fotos/{fotoId}/estado — Consultar estado de procesamiento */
  consultarEstadoFoto(productoId: number, fotoId: number): Observable<ProductoFoto> {
    return this.http.get<ProductoFoto>(`${this.apiUrl}/${productoId}/fotos/${fotoId}/estado`);
  }

  /** PUT /api/v1/productos/{productoId}/fotos/{fotoId}/principal — Establecer como principal */
  establecerPrincipal(productoId: number, fotoId: number): Observable<ProductoFoto> {
    return this.http.put<ProductoFoto>(`${this.apiUrl}/${productoId}/fotos/${fotoId}/principal`, {});
  }

  /** DELETE /api/v1/productos/{productoId}/fotos/{fotoId} — Eliminar foto */
  eliminarFoto(productoId: number, fotoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productoId}/fotos/${fotoId}`);
  }

  /** POST /api/v1/productos/{productoId}/fotos/{fotoId}/reintentar — Reintentar foto en error */
  reintentarFoto(productoId: number, fotoId: number): Observable<ProductoFoto> {
    return this.http.post<ProductoFoto>(`${this.apiUrl}/${productoId}/fotos/${fotoId}/reintentar`, {});
  }
}
