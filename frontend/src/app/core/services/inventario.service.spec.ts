import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { InventarioService } from './inventario.service';
import { InventarioMovimientoRequest, AjusteInventarioRequest } from '../models/inventario.model';

describe('InventarioService', () => {
  let service: InventarioService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InventarioService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(InventarioService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should register an inventory entry via POST /inventario/entradas', () => {
    const request: InventarioMovimientoRequest = {
      productoId: 10,
      cantidad: 15,
      motivo: 'Compra de surtido semanal',
      costoUnitario: 25.5,
    };

    service.registrarEntrada(request).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/inventario/entradas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 100, ...request });
  });

  it('should register an inventory exit via POST /inventario/salidas', () => {
    const request: InventarioMovimientoRequest = {
      productoId: 10,
      cantidad: 3,
      motivo: 'Muestra de producto o merma',
      costoUnitario: 25.5,
    };

    service.registrarSalida(request).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/inventario/salidas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 101, ...request });
  });

  it('should adjust inventory via POST /productos/ajustar-inventario', () => {
    const request: AjusteInventarioRequest = {
      productoId: 10,
      cantidad: 50,
      motivo: 'Conteo físico inicial de inventario',
      esFijarStockAbsoluto: true,
    };

    service.ajustarInventario(request).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/ajustar-inventario');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 10, stockActual: 50, cantidadDesconocida: false });
  });

  it('should request movements with filters and pagination via GET /inventario/movimientos', () => {
    service
      .obtenerMovimientos(
        {
          productoId: 10,
          tipo: 'ENTRADA',
          usuarioId: 1,
        },
        0,
        20
      )
      .subscribe();

    const req = httpTesting.expectOne(
      (r) =>
        r.url === 'http://localhost:8080/api/v1/inventario/movimientos' &&
        r.params.get('productoId') === '10' &&
        r.params.get('tipo') === 'ENTRADA' &&
        r.params.get('usuarioId') === '1' &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '20'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: { size: 20, number: 0, totalElements: 0, totalPages: 0 } });
  });
});
