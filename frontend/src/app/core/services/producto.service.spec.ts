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

  it('should request /buscar with advanced filters', () => {
    const service = TestBed.inject(ProductoService);
    service.buscarAvanzado({
      termino: 'pluma',
      categoriaId: 2,
      proveedorId: 3,
      activo: true,
      soloStockBajo: true,
      precioMin: 10,
      precioMax: 50,
    }, 1, 15).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      (req) =>
        req.url === 'http://localhost:8080/api/v1/productos/buscar' &&
        req.params.get('termino') === 'pluma' &&
        req.params.get('categoriaId') === '2' &&
        req.params.get('proveedorId') === '3' &&
        req.params.get('activo') === 'true' &&
        req.params.get('soloStockBajo') === 'true' &&
        req.params.get('precioMin') === '10' &&
        req.params.get('precioMax') === '50' &&
        req.params.get('page') === '1' &&
        req.params.get('size') === '15'
    );
    request.flush({ content: [], page: { size: 15, number: 1, totalElements: 0, totalPages: 0 } });
  });

  it('should get product detail by id', () => {
    const service = TestBed.inject(ProductoService);
    service.obtenerPorId(10).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/productos/10'
    );
    expect(request.request.method).toBe('GET');
    request.flush({ id: 10, nombre: 'Producto 10' });
  });

  it('should create product via POST', () => {
    const service = TestBed.inject(ProductoService);
    const dto = {
      codigoBarras: 'ABC123',
      nombre: 'Nuevo',
      categoriaId: 1,
      costoCompra: 20,
      unidadMedida: 'PIEZA',
    };
    service.crear(dto).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/productos'
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(dto);
    request.flush({ id: 1, ...dto });
  });

  it('should update product via PUT', () => {
    const service = TestBed.inject(ProductoService);
    const dto = {
      codigoBarras: 'ABC123',
      nombre: 'Actualizado',
      categoriaId: 1,
      costoCompra: 25,
      stockMinimo: 10,
      unidadMedida: 'PIEZA',
    };
    service.actualizar(1, dto).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/productos/1 Persona'.replace(' Persona', '')
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(dto);
    request.flush({ id: 1, ...dto });
  });

  it('should deactivate product via PATCH', () => {
    const service = TestBed.inject(ProductoService);
    service.desactivar(5).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/productos/5/desactivar'
    );
    expect(request.request.method).toBe('PATCH');
    request.flush({ id: 5, activo: false });
  });

  it('should reactivate product via PATCH', () => {
    const service = TestBed.inject(ProductoService);
    service.reactivar(5).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/productos/5/reactivar'
    );
    expect(request.request.method).toBe('PATCH');
    request.flush({ id: 5, activo: true });
  });
});
