import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProductoFotosModalComponent } from './producto-fotos-modal.component';
import { ProductoFotoService } from '../../../core/services/producto-foto.service';
import { ProductoFoto } from '../../../core/models/producto-foto.model';

describe('ProductoFotosModalComponent', () => {
  const mockFotos: ProductoFoto[] = [
    {
      id: 1,
      nombreArchivo: 'cuaderno-frente.jpg',
      contentType: 'image/jpeg',
      tamanio: 102400,
      esPrincipal: true,
      orden: 0,
      fechaSubida: '2026-09-04T10:00:00',
      urlOriginal: '/api/v1/productos/10/fotos/1',
      urlThumbnail: '/api/v1/productos/10/fotos/1?size=200',
      estadoProcesamiento: 'COMPLETADO',
      mensajeError: null,
    },
    {
      id: 2,
      nombreArchivo: 'cuaderno-reverso.jpg',
      contentType: 'image/jpeg',
      tamanio: 204800,
      esPrincipal: false,
      orden: 1,
      fechaSubida: '2026-09-04T10:05:00',
      urlOriginal: '/api/v1/productos/10/fotos/2',
      urlThumbnail: '/api/v1/productos/10/fotos/2?size=200',
      estadoProcesamiento: 'ERROR',
      mensajeError: 'Imagen truncada o dañada',
    },
  ];

  const fotoServiceMock = {
    listarFotos: vi.fn().mockReturnValue(of(mockFotos)),
    subirFoto: vi.fn().mockReturnValue(of({ ...mockFotos[0], id: 3, estadoProcesamiento: 'PENDIENTE' })),
    consultarEstadoFoto: vi.fn().mockReturnValue(of(mockFotos[0])),
    establecerPrincipal: vi.fn().mockReturnValue(of(mockFotos[1])),
    eliminarFoto: vi.fn().mockReturnValue(of(void 0)),
    reintentarFoto: vi.fn().mockReturnValue(of({ ...mockFotos[1], estadoProcesamiento: 'PROCESANDO' })),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    fotoServiceMock.listarFotos.mockReturnValue(of(mockFotos));

    await TestBed.configureTestingModule({
      imports: [ProductoFotosModalComponent],
      providers: [{ provide: ProductoFotoService, useValue: fotoServiceMock }],
    }).compileComponents();
  });

  it('should list existing photos and show badges', () => {
    const fixture = TestBed.createComponent(ProductoFotosModalComponent);
    fixture.componentRef.setInput('productoId', 10);
    fixture.componentRef.setInput('productoNombre', 'Cuaderno Profesional');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('cuaderno-frente.jpg');
    expect(compiled.textContent).toContain('cuaderno-reverso.jpg');
    expect(compiled.textContent).toContain('★ Principal');
    expect(compiled.textContent).toContain('Fallo de procesamiento');
    expect(fotoServiceMock.listarFotos).toHaveBeenCalledWith(10);
  });

  it('should reject file exceeding 4 MB limit with validation error message', () => {
    const fixture = TestBed.createComponent(ProductoFotosModalComponent);
    fixture.componentRef.setInput('productoId', 10);
    fixture.componentRef.setInput('productoNombre', 'Cuaderno Profesional');
    fixture.detectChanges();

    const component = fixture.componentInstance;
    const bigFile = new File(['x'], 'too-big.jpg', { type: 'image/jpeg' });
    Object.defineProperty(bigFile, 'size', { value: 5 * 1024 * 1024 });

    const event = { target: { files: [bigFile], value: 'fake' } } as unknown as Event;
    component.onArchivoSeleccionado(event);

    expect(component.archivoSeleccionado()).toBeNull();
    expect(component.mensajeError()).toContain('excede el tamaño máximo permitido de 4 MB');
  });

  it('should upload valid file and notify update', () => {
    const fixture = TestBed.createComponent(ProductoFotosModalComponent);
    fixture.componentRef.setInput('productoId', 10);
    fixture.componentRef.setInput('productoNombre', 'Cuaderno Profesional');
    fixture.detectChanges();

    const component = fixture.componentInstance;
    const validFile = new File(['valid'], 'portada.png', { type: 'image/png' });
    Object.defineProperty(validFile, 'size', { value: 500 * 1024 });

    const event = { target: { files: [validFile], value: 'fake' } } as unknown as Event;
    component.onArchivoSeleccionado(event);
    expect(component.archivoSeleccionado()).toBe(validFile);

    let emitido = false;
    component.fotosActualizadas.subscribe(() => (emitido = true));

    component.subirArchivo();
    expect(fotoServiceMock.subirFoto).toHaveBeenCalledWith(10, validFile, false);
    expect(emitido).toBe(true);
  });

  it('should establish photo as principal and call service', () => {
    const fixture = TestBed.createComponent(ProductoFotosModalComponent);
    fixture.componentRef.setInput('productoId', 10);
    fixture.componentRef.setInput('productoNombre', 'Cuaderno Profesional');
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.establecerComoPrincipal(2);

    expect(fotoServiceMock.establecerPrincipal).toHaveBeenCalledWith(10, 2);
  });

  it('should delete photo and refresh list', () => {
    const fixture = TestBed.createComponent(ProductoFotosModalComponent);
    fixture.componentRef.setInput('productoId', 10);
    fixture.componentRef.setInput('productoNombre', 'Cuaderno Profesional');
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.eliminarFoto(1);

    expect(fotoServiceMock.eliminarFoto).toHaveBeenCalledWith(10, 1);
  });
});
