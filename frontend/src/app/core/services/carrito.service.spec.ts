import { TestBed } from '@angular/core/testing';
import { ClienteResumen } from '../models/cliente.model';
import { ProductoListado } from '../models/producto.model';
import { CarritoService } from './carrito.service';

function producto(overrides: Partial<ProductoListado> = {}): ProductoListado {
  return {
    id: 1,
    codigoBarras: '750100011',
    nombre: 'Cuaderno profesional',
    categoriaNombre: 'Papelería',
    proveedorNombre: 'Distribuidora',
    precioVenta: 30,
    stockActual: 5,
    stockMinimo: 1,
    activo: true,
    cantidadDesconocida: false,
    urlThumbnail: null,
    tieneFotos: false,
    ...overrides,
  };
}

describe('CarritoService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [CarritoService] });
  });

  it('should add a new product as a line with cantidad 1', () => {
    const service = TestBed.inject(CarritoService);

    service.agregarProducto(producto());

    expect(service.lineas()).toEqual([expect.objectContaining({ productoId: 1, cantidad: 1 })]);
  });

  it('should increment cantidad when adding the same product again', () => {
    const service = TestBed.inject(CarritoService);

    service.agregarProducto(producto());
    service.agregarProducto(producto());

    expect(service.lineas().length).toBe(1);
    expect(service.lineas()[0].cantidad).toBe(2);
  });

  it('should clamp cantidad to the known stock', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto({ stockActual: 3 }));

    service.establecerCantidad(1, 10);

    expect(service.lineas()[0].cantidad).toBe(3);
  });

  it('should not clamp cantidad when stock is unknown', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto({ cantidadDesconocida: true, stockActual: 0 }));

    service.establecerCantidad(1, 500);

    expect(service.lineas()[0].cantidad).toBe(500);
  });

  it('should remove the line when decrementing from 1', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto());

    service.decrementar(1);

    expect(service.lineas()).toEqual([]);
  });

  it('should decrement without removing when cantidad is greater than 1', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto());
    service.incrementar(1);

    service.decrementar(1);

    expect(service.lineas()[0].cantidad).toBe(1);
  });

  it('should remove a line explicitly', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto());

    service.eliminar(1);

    expect(service.lineas()).toEqual([]);
  });

  it('should derive subtotalEstimado and totalArticulos reactively', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto({ id: 1, precioVenta: 30 }));
    service.agregarProducto(producto({ id: 2, precioVenta: 20, nombre: 'Otro' }));
    service.establecerCantidad(2, 2);

    expect(service.subtotalEstimado()).toBe(30 * 1 + 20 * 2);
    expect(service.totalArticulos()).toBe(3);
  });

  it('should default clienteSeleccionado to null (mostrador) and allow selecting one', () => {
    const service = TestBed.inject(CarritoService);
    const cliente: ClienteResumen = { id: 2, nombre: 'Ana', telefono: '555', totalCompras: 0, nivel: 'Regular' };

    expect(service.clienteSeleccionado()).toBeNull();

    service.seleccionarCliente(cliente);
    expect(service.clienteSeleccionado()).toEqual(cliente);

    service.seleccionarCliente(null);
    expect(service.clienteSeleccionado()).toBeNull();
  });

  it('should clear everything on vaciar', () => {
    const service = TestBed.inject(CarritoService);
    service.agregarProducto(producto());
    service.seleccionarCliente({ id: 2, nombre: 'Ana', telefono: '555', totalCompras: 0, nivel: 'Regular' });

    service.vaciar();

    expect(service.lineas()).toEqual([]);
    expect(service.clienteSeleccionado()).toBeNull();
  });
});
