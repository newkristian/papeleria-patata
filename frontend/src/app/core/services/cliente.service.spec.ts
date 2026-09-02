import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ClienteResumen } from '../models/cliente.model';
import { ClienteService } from './cliente.service';

describe('ClienteService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClienteService, provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('should GET the full client list', () => {
    const service = TestBed.inject(ClienteService);
    const clientes: ClienteResumen[] = [
      { id: 2, nombre: 'Cliente registrado', telefono: '555-0000', totalCompras: 100, nivel: 'Regular' },
    ];

    service.listar().subscribe((resultado) => {
      expect(resultado).toEqual(clientes);
    });

    const request = TestBed.inject(HttpTestingController).expectOne('http://localhost:8080/api/v1/clientes');
    expect(request.request.method).toBe('GET');
    request.flush(clientes);
  });
});
