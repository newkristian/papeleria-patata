import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VentaRequest, VentaResponse } from '../models/venta.model';

@Injectable({
  providedIn: 'root',
})
export class VentaService {
  private readonly apiUrl = `${environment.apiUrl}/api/v1/ventas`;

  constructor(private http: HttpClient) {}

  /** POST /ventas — confirma la venta. El backend recalcula precios, promociones y totales. */
  crear(request: VentaRequest): Observable<VentaResponse> {
    return this.http.post<VentaResponse>(this.apiUrl, request);
  }
}
