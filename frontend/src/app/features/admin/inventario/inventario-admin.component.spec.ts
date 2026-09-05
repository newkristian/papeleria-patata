import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { InventarioAdminComponent } from './inventario-admin.component';
import { InventarioService } from '../../../core/services/inventario.service';
import { ProductoService } from '../../../core/services/producto.service';
import { AuthService } from '../../../core/services/auth.service';
import { InventarioMovimiento } from '../../../core/models/inventario.model';
import { Pagina, ProductoDetalle, ProductoListado } from '../../../core/models/producto.model';

describe('InventarioAdminComponent', () => {
  const mockProductosPorContar: ProductoListado[] = [
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

  const mockProductosNormales: ProductoListado[] = [
    {
      id: 1,
      codigoBarras: '75010001',
      nombre: 'Cuaderno Profesional Raya',
      categoriaNombre: 'Papelería General',
      proveedorNombre: 'Scribe',
      precioVenta: 25.5,
      stockActual: 20,
      stockMinimo: 5,
      activo: true,
      cantidadDesconocida: false,
      urlThumbnail: null,
      tieneFotos: false,
    },
  ];

  const mockMovimientos: InventarioMovimiento[] = [
    {
      id: 101,
      productoId: 1,
      productoNombre: 'Cuaderno Profesional Raya',
      productoCodigoBarras: '75010001',
      usuarioId: 10,
      usuarioNombre: 'Admin General',
      tipo: 'ENTRADA',
      cantidad: 15,
      motivo: 'Compra factura 99',
      costoUnitario: 14.5,
      fechaMovimiento: '2026-09-04T10:00:00',
    },
    {
      id: 102,
      productoId: 1,
      productoNombre: 'Cuaderno Profesional Raya',
      productoCodigoBarras: '75010001',
      usuarioId: 10,
      usuarioNombre: 'Admin General',
      tipo: 'SALIDA',
      cantidad: 2,
      motivo: 'Muestra comercial',
      costoUnitario: 14.5,
      fechaMovimiento: '2026-09-04T11:00:00',
    },
  ];

  const mockPaginaMovimientos: Pagina<InventarioMovimiento> = {
    content: mockMovimientos,
    page: {
      size: 20,
      number: 0,
      totalElements: 2,
      totalPages: 1,
    },
  };

  const mockDetalleProducto: ProductoDetalle = {
    id: 1,
    codigoBarras: '75010001',
    nombre: 'Cuaderno Profesional Raya',
    descripcion: '100 hojas',
    categoria: { id: 1, nombre: 'Papelería General', descripcion: null },
    proveedor: {
      id: 1,
      nombre: 'Scribe',
      rfc: 'DSC980101XYZ',
      porcentajeComision: 10,
      telefono: '5512345678',
      contacto: 'Juan',
      email: 'juan@scribe.com',
      activo: true,
      sistema: false,
    },
    costoCompra: 14.5,
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

  const inventarioServiceMock = {
    listarMovimientos: vi.fn().mockReturnValue(of(mockPaginaMovimientos)),
    registrarEntrada: vi.fn().mockReturnValue(of(mockMovimientos[0])),
    registrarSalida: vi.fn().mockReturnValue(of(mockMovimientos[1])),
    ajustarInventario: vi.fn().mockReturnValue(of({ ...mockDetalleProducto, stockActual: 30 })),
  };

  const productoServiceMock = {
    buscarAvanzado: vi.fn().mockReturnValue(of({
      content: [...mockProductosNormales, ...mockProductosPorContar],
      page: { size: 50, number: 0, totalElements: 2, totalPages: 1 },
    })),
    obtenerPorId: vi.fn().mockReturnValue(of(mockDetalleProducto)),
  };

  const authServiceMock = {
    canManageProducts: () => true,
    isVendedor: () => false,
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    inventarioServiceMock.listarMovimientos.mockReturnValue(of(mockPaginaMovimientos));
    productoServiceMock.buscarAvanzado.mockReturnValue(of({
      content: [...mockProductosNormales, ...mockProductosPorContar],
      page: { size: 50, number: 0, totalElements: 2, totalPages: 1 },
    }));
    productoServiceMock.obtenerPorId.mockReturnValue(of(mockDetalleProducto));
    inventarioServiceMock.registrarEntrada.mockReturnValue(of(mockMovimientos[0]));
    inventarioServiceMock.registrarSalida.mockReturnValue(of(mockMovimientos[1]));
    inventarioServiceMock.ajustarInventario.mockReturnValue(of({ ...mockDetalleProducto, stockActual: 30 }));

    await TestBed.configureTestingModule({
      imports: [InventarioAdminComponent],
      providers: [
        { provide: InventarioService, useValue: inventarioServiceMock },
        { provide: ProductoService, useValue: productoServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should render movement table and attention banner for products with unknown stock', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Control de Inventario');
    expect(compiled.textContent).toContain('Productos Pendientes de Conteo Físico Inicial (1)');
    expect(compiled.textContent).toContain('Lápiz Mirado 2');
    expect(compiled.textContent).toContain('Conteo inicial');

    expect(compiled.textContent).toContain('Cuaderno Profesional Raya');
    expect(compiled.textContent).toContain('Entrada');
    expect(compiled.textContent).toContain('Salida');
    expect(compiled.textContent).toContain('+15');
    expect(compiled.textContent).toContain('-2');
    expect(compiled.textContent).toContain('$14.50');
  });

  it('should open initial count adjustment modal with prefilled data when clicking "Conteo inicial"', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const prodPorContar = mockProductosPorContar[0];
    component.iniciarConteoInicial(prodPorContar);

    expect(component.modalMovimientoAbierto()).toBe(true);
    expect(component.tipoMovimientoModal()).toBe('AJUSTE');
    expect(component.productoSeleccionado()).toEqual(prodPorContar);
    expect(component.formAjuste.get('productoId')?.value).toBe(prodPorContar.id);
    expect(component.formAjuste.get('esFijarStockAbsoluto')?.value).toBe(true);
    expect(component.formAjuste.get('motivo')?.value).toBe('Conteo físico inicial de inventario');
  });

  it('should submit absolute stock adjustment successfully', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.iniciarConteoInicial(mockProductosPorContar[0]);
    component.formAjuste.patchValue({
      cantidad: 30,
      motivo: 'Conteo físico verificado',
    });

    component.guardarAjuste();

    expect(inventarioServiceMock.ajustarInventario).toHaveBeenCalledWith({
      productoId: 2,
      cantidad: 30,
      motivo: 'Conteo físico verificado',
      nuevoCostoCompra: null,
      esFijarStockAbsoluto: true,
    });
    expect(component.modalMovimientoAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('Ajuste de inventario aplicado');
  });

  it('should register manual entry (ENTRADA) successfully', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.abrirModalMovimiento('ENTRADA', mockProductosNormales[0]);
    expect(component.formMovimiento.get('productoId')?.value).toBe(1);

    component.formMovimiento.patchValue({
      cantidad: 15,
      costoUnitario: 14.5,
      motivo: 'Compra factura 99',
    });

    component.guardarMovimiento();

    expect(inventarioServiceMock.registrarEntrada).toHaveBeenCalledWith({
      productoId: 1,
      cantidad: 15,
      costoUnitario: 14.5,
      motivo: 'Compra factura 99',
    });
    expect(component.modalMovimientoAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('Movimiento de ENTRADA registrado');
  });

  it('should validate and register manual exit (SALIDA) successfully', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.abrirModalMovimiento('SALIDA', mockProductosNormales[0]);
    component.formMovimiento.patchValue({
      cantidad: 2,
      costoUnitario: 14.5,
      motivo: 'Muestra comercial',
    });

    expect(component.esSalidaInvalida()).toBe(false);

    component.guardarMovimiento();

    expect(inventarioServiceMock.registrarSalida).toHaveBeenCalledWith({
      productoId: 1,
      cantidad: 2,
      costoUnitario: 14.5,
      motivo: 'Muestra comercial',
    });
    expect(component.modalMovimientoAbierto()).toBe(false);
  });

  it('should prevent SALIDA if quantity exceeds current stock or if stock is unknown', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    // Case 1: Exceeds stock (stock is 20, asks 50)
    component.abrirModalMovimiento('SALIDA', mockProductosNormales[0]);
    component.formMovimiento.patchValue({ cantidad: 50 });
    expect(component.esSalidaInvalida()).toBe(true);

    // Case 2: Unknown stock
    component.abrirModalMovimiento('SALIDA', mockProductosPorContar[0]);
    component.formMovimiento.patchValue({ cantidad: 1 });
    expect(component.esSalidaInvalida()).toBe(true);
  });

  it('should filter movements by type', () => {
    const fixture = TestBed.createComponent(InventarioAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onTipoChange({ target: { value: 'ENTRADA' } } as unknown as Event);

    expect(component.filtroTipo()).toBe('ENTRADA');
    expect(inventarioServiceMock.listarMovimientos).toHaveBeenCalledWith(
      expect.objectContaining({ tipo: 'ENTRADA' }),
      0,
      20
    );
  });
});
