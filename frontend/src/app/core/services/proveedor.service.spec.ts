import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProveedorService } from './proveedor.service';
import { Proveedor, ProveedorRequest } from '../models/proveedor.model';
import { Pagina } from '../models/producto.model';

describe('ProveedorService', () => {
  let service: ProveedorService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProveedorService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ProveedorService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should list all providers without search query', () => {
    const mockList: Proveedor[] = [
      {
        id: 1,
        nombre: 'Proveedor Lista',
        rfc: null,
        telefono: null,
        email: null,
        contacto: null,
        porcentajeComision: 10,
        activo: true,
        sistema: false,
      },
    ];

    service.listarTodos().subscribe((data) => {
      expect(data).toEqual(mockList);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/proveedores');
    expect(req.request.method).toBe('GET');
    req.flush(mockList);
  });

  it('should search providers with pagination and query filters', () => {
    const mockPagina: Pagina<Proveedor> = {
      content: [
        {
          id: 1,
          nombre: 'Distribuidora Central',
          rfc: 'DCE123456789',
          telefono: '5551234',
          email: 'contacto@distribuidora.com',
          contacto: 'Juan',
          porcentajeComision: 10,
          activo: true,
          sistema: false,
        },
      ],
      page: { size: 15, number: 0, totalElements: 1, totalPages: 1 },
    };

    service.buscar('Distribuidora', true, 0, 15).subscribe((data) => {
      expect(data).toEqual(mockPagina);
    });

    const req = httpTesting.expectOne(
      (r) =>
        r.url === 'http://localhost:8080/api/v1/proveedores/buscar' &&
        r.params.get('termino') === 'Distribuidora' &&
        r.params.get('activo') === 'true' &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '15'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockPagina);
  });

  it('should get count of associated products for provider', () => {
    service.contarProductosAsignados(5).subscribe((count) => {
      expect(count).toBe(12);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/proveedores/5/productos/conteo');
    expect(req.request.method).toBe('GET');
    req.flush(12);
  });

  it('should create a provider', () => {
    const request: ProveedorRequest = {
      nombre: 'Nuevo Proveedor S.A.',
      porcentajeComision: 15,
    };
    const created: Proveedor = {
      id: 10,
      nombre: 'Nuevo Proveedor S.A.',
      porcentajeComision: 15,
      activo: true,
      sistema: false,
    };

    service.crear(request).subscribe((data) => {
      expect(data).toEqual(created);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/proveedores');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(created);
  });

  it('should deactivate a provider via DELETE', () => {
    service.desactivar(10).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/proveedores/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
