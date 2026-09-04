import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CategoriaService } from './categoria.service';
import { Categoria, CategoriaRequest } from '../models/categoria.model';

describe('CategoriaService', () => {
  let service: CategoriaService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CategoriaService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(CategoriaService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should list all categories', () => {
    const mockCategorias: Categoria[] = [
      { id: 1, nombre: 'Papelería', descripcion: 'Artículos de papel' },
      { id: 2, nombre: 'Escritura', descripcion: 'Bolígrafos y lápices' },
    ];

    service.listarTodas().subscribe((data) => {
      expect(data).toEqual(mockCategorias);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/categorias');
    expect(req.request.method).toBe('GET');
    req.flush(mockCategorias);
  });

  it('should create a new category', () => {
    const request: CategoriaRequest = { nombre: 'Arte', descripcion: 'Pinturas' };
    const created: Categoria = { id: 3, ...request };

    service.crear(request).subscribe((data) => {
      expect(data).toEqual(created);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/categorias');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(created);
  });

  it('should update a category', () => {
    const request: CategoriaRequest = { nombre: 'Arte y Dibujo', descripcion: 'Pinturas y lienzos' };
    const updated: Categoria = { id: 3, ...request };

    service.actualizar(3, request).subscribe((data) => {
      expect(data).toEqual(updated);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/categorias/3');
    expect(req.request.method).toBe('PUT');
    req.flush(updated);
  });

  it('should delete a category', () => {
    service.eliminar(3).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/categorias/3');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
