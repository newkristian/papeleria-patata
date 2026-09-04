import { TestBed } from '@angular/core/testing';
import { ProveedoresAdminComponent } from './proveedores-admin.component';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { AuthService } from '../../../core/services/auth.service';
import { of } from 'rxjs';
import { Proveedor } from '../../../core/models/proveedor.model';
import { Pagina } from '../../../core/models/producto.model';

describe('ProveedoresAdminComponent', () => {
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
      porcentajeComision: 12.5,
      telefono: '5512345678',
      contacto: 'Juan Pérez',
      email: 'juan@scribe.com',
      activo: true,
      sistema: false,
    },
    {
      id: 3,
      nombre: 'Papelera Inactiva SA',
      rfc: null,
      porcentajeComision: 5.0,
      telefono: null,
      contacto: null,
      email: null,
      activo: false,
      sistema: false,
    },
  ];

  const mockPagina: Pagina<Proveedor> = {
    content: mockProveedores,
    page: {
      size: 15,
      number: 0,
      totalElements: 3,
      totalPages: 1,
    },
  };

  const proveedorServiceMock = {
    listarTodos: vi.fn().mockReturnValue(of(mockProveedores)),
    buscar: vi.fn().mockReturnValue(of(mockPagina)),
    crear: vi.fn().mockReturnValue(of({ ...mockProveedores[1], id: 4 })),
    actualizar: vi.fn().mockReturnValue(of(mockProveedores[1])),
    desactivar: vi.fn().mockReturnValue(of(void 0)),
    contarProductosAsignados: vi.fn().mockReturnValue(of(5)),
  };

  const authServiceMock = {
    canManageProviders: () => true,
    canDeactivateProviders: () => true,
    isAdmin: () => true,
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    proveedorServiceMock.listarTodos.mockReturnValue(of(mockProveedores));
    proveedorServiceMock.buscar.mockReturnValue(of(mockPagina));

    await TestBed.configureTestingModule({
      imports: [ProveedoresAdminComponent],
      providers: [
        { provide: ProveedorService, useValue: proveedorServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should load initial list using listarTodos without calling buscar on empty search', () => {
    const fixture = TestBed.createComponent(ProveedoresAdminComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('PENDIENTE');
    expect(compiled.textContent).toContain('SISTEMA');
    expect(compiled.textContent).toContain('Distribuidora Scribe');
    expect(compiled.textContent).toContain('Papelera Inactiva SA');
    expect(proveedorServiceMock.listarTodos).toHaveBeenCalled();
    expect(proveedorServiceMock.buscar).not.toHaveBeenCalled();
  });

  it('should call buscar endpoint only when search input is not empty', () => {
    const fixture = TestBed.createComponent(ProveedoresAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    // Al inicio con input vacío, se usó listarTodos y NO buscar
    expect(proveedorServiceMock.buscar).not.toHaveBeenCalled();

    // Cuando el usuario escribe un término, se invoca buscar
    const inputEvent = { target: { value: 'Scribe' } } as unknown as Event;
    component.onTerminoChange(inputEvent);

    expect(component.terminoBusqueda()).toBe('Scribe');
    expect(component.paginaActual()).toBe(0);
    expect(proveedorServiceMock.buscar).toHaveBeenCalledWith('Scribe', null, 0, 15);
  });

  it('should filter providers on status dropdown change using listarTodos when search is empty', () => {
    const fixture = TestBed.createComponent(ProveedoresAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const selectEvent = { target: { value: 'activos' } } as unknown as Event;
    component.onFiltroActivoChange(selectEvent);

    expect(component.filtroActivo()).toBe(true);
    expect(component.paginaActual()).toBe(0);
    expect(proveedorServiceMock.listarTodos).toHaveBeenCalled();
    expect(proveedorServiceMock.buscar).not.toHaveBeenCalled();
  });

  it('should open modal for new provider and submit successfully', () => {
    const fixture = TestBed.createComponent(ProveedoresAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.modalAbierto()).toBe(false);

    component.abrirModalCrear();
    expect(component.modalAbierto()).toBe(true);
    expect(component.proveedorEnEdicion()).toBeNull();

    component.form.patchValue({
      nombre: 'Nuevo Proveedor Papel',
      rfc: 'NPP900101AA1',
      porcentajeComision: 15,
      telefono: '5551234567',
      contacto: 'Laura Gómez',
      email: 'laura@proveedor.com',
    });

    component.guardarProveedor();

    expect(proveedorServiceMock.crear).toHaveBeenCalledWith(
      expect.objectContaining({
        nombre: 'Nuevo Proveedor Papel',
        rfc: 'NPP900101AA1',
        porcentajeComision: 15,
      })
    );
    expect(component.modalAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('registrado exitosamente');
  });

  it('should open modal for editing an existing provider and submit update', () => {
    const fixture = TestBed.createComponent(ProveedoresAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const proveedorAEditar = mockProveedores[1];
    component.abrirModalEditar(proveedorAEditar);

    expect(component.modalAbierto()).toBe(true);
    expect(component.proveedorEnEdicion()).toEqual(proveedorAEditar);
    expect(component.form.value.nombre).toBe('Distribuidora Scribe');

    component.form.patchValue({
      porcentajeComision: 14,
    });

    component.guardarProveedor();

    expect(proveedorServiceMock.actualizar).toHaveBeenCalledWith(
      2,
      expect.objectContaining({
        nombre: 'Distribuidora Scribe',
        porcentajeComision: 14,
      })
    );
    expect(component.modalAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('actualizado exitosamente');
  });

  it('should open deactivation dialog showing affected products count and execute deactivation', () => {
    const fixture = TestBed.createComponent(ProveedoresAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const proveedorADesactivar = mockProveedores[1];
    component.iniciarDesactivacion(proveedorADesactivar);

    expect(proveedorServiceMock.contarProductosAsignados).toHaveBeenCalledWith(2);
    expect(component.conteoProductosAfectados()).toBe(5);
    expect(component.modalDesactivacion()).toBe(true);
    expect(component.proveedorParaDesactivar()).toEqual(proveedorADesactivar);

    component.ejecutarDesactivacion();

    expect(proveedorServiceMock.desactivar).toHaveBeenCalledWith(2);
    expect(component.modalDesactivacion()).toBe(false);
    expect(component.mensajeExito()).toContain('reasignados a PENDIENTE');
  });
});
