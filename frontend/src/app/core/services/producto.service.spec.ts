import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Pagina, ProductoListado } from '../models/producto.model';
import { ProductoService } from './producto.service';

describe('ProductoService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductoService, provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('should request /buscar with termino, page and size as query params', () => {
    const service = TestBed.inject(ProductoService);
    const pagina: Pagina<ProductoListado> = {
      content: [],
      page: { size: 20, number: 0, totalElements: 0, totalPages: 0 },
    };

    service.buscar('cuaderno', 0, 20).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      (req) =>
        req.url === 'http://localhost:8080/api/v1/productos/buscar' &&
        req.params.get('termino') === 'cuaderno' &&
        req.params.get('page') === '0' &&
        req.params.get('size') === '20',
    );
    request.flush(pagina);
  });

  it('should omit termino from the request when it is empty', () => {
    const service = TestBed.inject(ProductoService);

    service.buscar('', 0, 20).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      (req) => req.url === 'http://localhost:8080/api/v1/productos/buscar',
    );
    expect(request.request.params.has('termino')).toBe(false);
    request.flush({ content: [], page: { size: 20, number: 0, totalElements: 0, totalPages: 0 } });
  });

  it('should request /codigo/{codigoBarras} url-encoded', () => {
    const service = TestBed.inject(ProductoService);
    const detalle = { id: 1, codigoBarras: '750/123' } as ProductoListado;

    service.buscarPorCodigoBarras('750/123').subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/productos/codigo/750%2F123',
    );
    request.flush(detalle);
  });
});
