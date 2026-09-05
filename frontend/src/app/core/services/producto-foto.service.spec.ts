import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductoFotoService } from './producto-foto.service';
import { ProductoFoto } from '../models/producto-foto.model';

describe('ProductoFotoService', () => {
  let service: ProductoFotoService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductoFotoService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductoFotoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should upload a photo using FormData via POST /productos/{id}/fotos', () => {
    const file = new File(['dummy-bytes'], 'foto.jpg', { type: 'image/jpeg' });
    const mockRes: ProductoFoto = {
      id: 1,
      nombreArchivo: 'foto.jpg',
      contentType: 'image/jpeg',
      tamanio: 1024,
      esPrincipal: true,
      orden: 0,
      fechaSubida: '2026-09-04T20:00:00',
      urlOriginal: '/api/v1/productos/10/fotos/1',
      urlThumbnail: '/api/v1/productos/10/fotos/1?size=200',
      estadoProcesamiento: 'PENDIENTE',
      mensajeError: null,
    };

    service.subirFoto(10, file, true).subscribe((data) => {
      expect(data).toEqual(mockRes);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/10/fotos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush(mockRes);
  });

  it('should list photos via GET /productos/{id}/fotos', () => {
    service.listarFotos(10).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/10/fotos');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should check photo processing status via GET /productos/{id}/fotos/{fotoId}/estado', () => {
    service.consultarEstadoFoto(10, 5).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/10/fotos/5/estado');
    expect(req.request.method).toBe('GET');
    req.flush({ id: 5, estadoProcesamiento: 'COMPLETADO' });
  });

  it('should set photo as principal via PUT /productos/{id}/fotos/{fotoId}/principal', () => {
    service.establecerPrincipal(10, 5).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/10/fotos/5/principal');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 5, esPrincipal: true });
  });

  it('should delete photo via DELETE /productos/{id}/fotos/{fotoId}', () => {
    service.eliminarFoto(10, 5).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/10/fotos/5');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should retry processing photo via POST /productos/{id}/fotos/{fotoId}/reintentar', () => {
    service.reintentarFoto(10, 5).subscribe();

    const req = httpTesting.expectOne('http://localhost:8080/api/v1/productos/10/fotos/5/reintentar');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 5, estadoProcesamiento: 'PROCESANDO' });
  });
});
