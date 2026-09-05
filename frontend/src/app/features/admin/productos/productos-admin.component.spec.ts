import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProductosAdminComponent } from './productos-admin.component';
import { ProductoService } from '../../../core/services/producto.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { AuthService } from '../../../core/services/auth.service';
import { Pagina, ProductoDetalle, ProductoListado } from '../../../core/models/producto.model';
import { Categoria } from '../../../core/models/categoria.model';
import { Proveedor } from '../../../core/models/proveedor.model';

describe('ProductosAdminComponent', () => {
  const mockCategorias: Categoria[] = [
    { id: 1, nombre: 'Papelería General', descripcion: 'Hojas y libretas' },
    { id: 2, nombre: 'Escritura', descripcion: 'Lápices y plumas' },
  ];

  const mockProveedores: Proveedor[] = [
    {
      id: 1,
      nombre: 'PENDIENTE',
      rfc: null,
      porcentajeComision: 0,
      telefono: null,
      contacto: null,
      email: null,
      activo: true,
      sistema: true,
    },
    {
      id: 2,
      nombre: 'Distribuidora Scribe',
      rfc: 'DSC980101XYZ',
      porcentajeComision: 10,
      telefono: '5512345678',
      contacto: 'Juan',
      email: 'juan@scribe.com',
      activo: true,
      sistema: false,
    },
  ];

  const mockProductos: ProductoListado[] = [
    {
      id: 1,
      codigoBarras: '75010001',
      nombre: 'Cuaderno Profesional Raya',
      categoriaNombre: 'Papelería General',
      proveedorNombre: 'Distribuidora Scribe',
      precioVenta: 25.5,
      stockActual: 20,
      stockMinimo: 5,
      activo: true,
      cantidadDesconocida: false,
      urlThumbnail: null,
      tieneFotos: false,
    },
    {
      id: 2,
      codigoBarras: '75010002',
      nombre: 'Lápiz Mirado 2',
      categoriaNombre: 'Escritura',
      proveedorNombre: 'PENDIENTE',
      precioVenta: 5.0,
      stockActual: 0,
      stockMinimo: 10,
      activo: true,
      cantidadDesconocida: true,
      urlThumbnail: null,
      tieneFotos: false,
    },
  ];

  const mockPagina: Pagina<ProductoListado> = {
    content: mockProductos,
    page: {
      size: 15,
      number: 0,
      totalElements: 2,
      totalPages: 1,
    },
  };

  const mockDetalle: ProductoDetalle = {
    id: 1,
    codigoBarras: '75010001',
    nombre: 'Cuaderno Profesional Raya',
    descripcion: '100 hojas',
    categoria: mockCategorias[0],
    proveedor: mockProveedores[1],
    costoCompra: 15.0,
    porcentajeGanancia: 70.0,
    precioVenta: 25.5,
    stockMinimo: 5,
    stockActual: 20,
    unidadMedida: 'PIEZA',
    activo: true,
    cantidadDesconocida: false,
    fechaCreacion: '2026-09-01T10:00:00',
    fechaActualizacion: '2026-09-01T10:00:00',
    fotos: [],
    fotoPrincipal: null,
  };

  const productoServiceMock = {
    buscarAvanzado: vi.fn().mockReturnValue(of(mockPagina)),
    obtenerPorId: vi.fn().mockReturnValue(of(mockDetalle)),
    crear: vi.fn().mockReturnValue(of(mockDetalle)),
    actualizar: vi.fn().mockReturnValue(of(mockDetalle)),
    desactivar: vi.fn().mockReturnValue(of({ ...mockDetalle, activo: false })),
    reactivar: vi.fn().mockReturnValue(of({ ...mockDetalle, activo: true })),
  };

  const categoriaServiceMock = {
    listarTodas: vi.fn().mockReturnValue(of(mockCategorias)),
  };

  const proveedorServiceMock = {
    listarTodos: vi.fn().mockReturnValue(of(mockProveedores)),
  };

  const authServiceMock = {
    canManageProducts: () => true,
    canManageProviders: () => true,
    canDeactivateProviders: () => true,
    isAdmin: () => true,
    isGerente: () => false,
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    productoServiceMock.buscarAvanzado.mockReturnValue(of(mockPagina));
    categoriaServiceMock.listarTodas.mockReturnValue(of(mockCategorias));
    proveedorServiceMock.listarTodos.mockReturnValue(of(mockProveedores));

    await TestBed.configureTestingModule({
      imports: [ProductosAdminComponent],
      providers: [
        { provide: ProductoService, useValue: productoServiceMock },
        { provide: CategoriaService, useValue: categoriaServiceMock },
        { provide: ProveedorService, useValue: proveedorServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should render products list including badges and price formatting', () => {
    const fixture = TestBed.createComponent(ProductosAdminComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Cuaderno Profesional Raya');
    expect(compiled.textContent).toContain('75010001');
    expect(compiled.textContent).toContain('Lápiz Mirado 2');
    expect(compiled.textContent).toContain('Por contar');
    expect(compiled.textContent).toContain('PENDIENTE');
    expect(productoServiceMock.buscarAvanzado).toHaveBeenCalled();
  });

  it('should search products on input change', () => {
    const fixture = TestBed.createComponent(ProductosAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const inputEvent = { target: { value: 'Cuaderno' } } as unknown as Event;
    component.onTerminoChange(inputEvent);

    expect(component.terminoBusqueda()).toBe('Cuaderno');
    expect(component.paginaActual()).toBe(0);
    expect(productoServiceMock.buscarAvanzado).toHaveBeenCalledWith(
      expect.objectContaining({ termino: 'Cuaderno' }),
      0,
      15
    );
  });

  it('should filter products on category and provider select changes', () => {
    const fixture = TestBed.createComponent(ProductosAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const catEvent = { target: { value: '1' } } as unknown as Event;
    component.onCategoriaChange(catEvent);

    expect(component.categoriaFiltro()).toBe(1);
    expect(productoServiceMock.buscarAvanzado).toHaveBeenCalledWith(
      expect.objectContaining({ categoriaId: 1 }),
      0,
      15
    );

    const provEvent = { target: { value: '2' } } as unknown as Event;
    component.onProveedorChange(provEvent);

    expect(component.proveedorFiltro()).toBe(2);
    expect(productoServiceMock.buscarAvanzado).toHaveBeenCalledWith(
      expect.objectContaining({ proveedorId: 2 }),
      0,
      15
    );
  });

  it('should open modal and create new product with unknown quantity', () => {
    const fixture = TestBed.createComponent(ProductosAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.abrirModalCrear();
    expect(component.modalAbierto()).toBe(true);

    component.form.patchValue({
      codigoBarras: 'ABC999',
      nombre: 'Borrador Migajón',
      categoriaId: 2,
      proveedorId: null,
      costoCompra: 3.5,
      stockMinimo: 5,
      unidadMedida: 'PIEZA',
      cantidadDesconocida: true,
    });

    component.guardarProducto();

    expect(productoServiceMock.crear).toHaveBeenCalledWith(
      expect.objectContaining({
        codigoBarras: 'ABC999',
        nombre: 'Borrador Migajón',
        categoriaId: 2,
        proveedorId: null,
        costoCompra: 3.5,
        cantidadDesconocida: true,
      })
    );
    expect(component.modalAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('registrado con precio de venta');
  });

  it('should open modal for editing, populate form, and submit update', () => {
    const fixture = TestBed.createComponent(ProductosAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.abrirModalEditar(1);
    expect(productoServiceMock.obtenerPorId).toHaveBeenCalledWith(1);
    expect(component.modalAbierto()).toBe(true);
    expect(component.form.value.codigoBarras).toBe('75010001');

    component.form.patchValue({
      nombre: 'Cuaderno Profesional Cuadro',
      costoCompra: 16.0,
    });

    component.guardarProducto();

    expect(productoServiceMock.actualizar).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        nombre: 'Cuaderno Profesional Cuadro',
        costoCompra: 16.0,
      })
    );
    expect(component.modalAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('actualizado con precio de venta');
  });

  it('should open deactivation dialog and execute deactivation', () => {
    const fixture = TestBed.createComponent(ProductosAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const prodADesactivar = mockProductos[0];
    component.abrirModalEstado(prodADesactivar, false);

    expect(component.modalEstado()).toBe(true);
    expect(component.productoParaEstado()).toEqual(prodADesactivar);
    expect(component.accionEstado()).toBe(false);

    component.ejecutarCambioEstado();

    expect(productoServiceMock.desactivar).toHaveBeenCalledWith(1);
    expect(component.modalEstado()).toBe(false);
    expect(component.mensajeExito()).toContain('desactivado exitosamente');
  });
});
