import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VentaRequest } from '../models/venta.model';
import { VentaService } from './venta.service';

describe('VentaService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [VentaService, provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('should POST the venta request as-is to /ventas', () => {
    const service = TestBed.inject(VentaService);
    const request: VentaRequest = {
      clienteId: null,
      metodoPago: 'EFECTIVO',
      detalles: [{ productoId: 1, cantidad: 2 }],
      carritoId: 'carrito-1',
    };

    service.crear(request).subscribe();

    const httpRequest = TestBed.inject(HttpTestingController).expectOne('http://localhost:8080/api/v1/ventas');
    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);
    httpRequest.flush({ id: 1, folio: 'POS-1' });
  });
});
