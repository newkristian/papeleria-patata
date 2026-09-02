import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SolicitudAutorizacionDescuento } from '../models/autorizacion-descuento.model';
import { AutorizacionDescuentoService } from './autorizacion-descuento.service';

describe('AutorizacionDescuentoService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AutorizacionDescuentoService, provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('should POST the solicitud as-is to /autorizaciones-descuento', () => {
    const service = TestBed.inject(AutorizacionDescuentoService);
    const solicitud: SolicitudAutorizacionDescuento = {
      username: 'gerente@pos.com',
      password: 'clave',
      productoId: 1,
      cantidad: 2,
      porcentaje: 10,
      motivo: 'Cliente frecuente',
      carritoId: 'carrito-1',
    };

    service.solicitar(solicitud).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/autorizaciones-descuento',
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(solicitud);
    request.flush({
      referencia: 'token',
      expiraEn: '2026-01-01T00:00:00',
      porcentaje: 10,
      montoDescuentoEstimado: 10,
      precioFinalEstimado: 90,
      promocionAutomaticaDisponible: false,
      montoPromocionAutomatica: 0,
      diferenciaVsPromocionAutomatica: 10,
    });
  });
});
