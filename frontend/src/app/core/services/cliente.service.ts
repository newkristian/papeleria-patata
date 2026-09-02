import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ClienteResumen } from '../models/cliente.model';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private readonly apiUrl = `${environment.apiUrl}/api/v1/clientes`;

  constructor(private http: HttpClient) {}

  /**
   * GET /clientes — lista completa, sin paginar ni filtrar en servidor (el backend no
   * ofrece búsqueda por nombre para este recurso). El filtrado en el selector del
   * carrito se hace sobre esta lista, del lado del cliente.
   */
  listar(): Observable<ClienteResumen[]> {
    return this.http.get<ClienteResumen[]>(this.apiUrl);
  }
}
