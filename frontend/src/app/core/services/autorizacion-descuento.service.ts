import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AutorizacionDescuentoResponse,
  SolicitudAutorizacionDescuento,
} from '../models/autorizacion-descuento.model';

@Injectable({
  providedIn: 'root',
})
export class AutorizacionDescuentoService {
  private readonly apiUrl = `${environment.apiUrl}/api/v1/autorizaciones-descuento`;

  constructor(private http: HttpClient) {}

  /**
   * POST /autorizaciones-descuento — reautentica al gerente/administrador con sus
   * propias credenciales (nunca las del vendedor que llama). La contraseña viaja solo
   * en este request; quien invoque este servicio debe limpiarla de su propio estado
   * inmediatamente después de llamarlo.
   */
  solicitar(solicitud: SolicitudAutorizacionDescuento): Observable<AutorizacionDescuentoResponse> {
    return this.http.post<AutorizacionDescuentoResponse>(this.apiUrl, solicitud);
  }
}
