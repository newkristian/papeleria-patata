import { TestBed } from '@angular/core/testing';
import { CategoriasAdminComponent } from './categorias-admin.component';
import { CategoriaService } from '../../../core/services/categoria.service';
import { AuthService } from '../../../core/services/auth.service';
import { of } from 'rxjs';
import { Categoria } from '../../../core/models/categoria.model';

describe('CategoriasAdminComponent', () => {
  const mockCategorias: Categoria[] = [
    { id: 1, nombre: 'Papelería General', descripcion: 'Hojas y libretas' },
    { id: 2, nombre: 'Escritura', descripcion: 'Lápices y plumas' },
  ];

  const categoriaServiceMock = {
    listarTodas: () => of(mockCategorias),
    crear: (cat: any) => of({ id: 3, ...cat }),
    actualizar: (id: number, cat: any) => of({ id, ...cat }),
    eliminar: (id: number) => of(void 0),
  };

  const authServiceMock = {
    canManageProducts: () => true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoriasAdminComponent],
      providers: [
        { provide: CategoriaService, useValue: categoriaServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should create and display categories list', () => {
    const fixture = TestBed.createComponent(CategoriasAdminComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Papelería General');
    expect(compiled.textContent).toContain('Escritura');
  });

  it('should open modal for new category and submit successfully', () => {
    const fixture = TestBed.createComponent(CategoriasAdminComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.modalAbierto()).toBe(false);

    component.abrirModalCrear();
    expect(component.modalAbierto()).toBe(true);

    component.form.patchValue({
      nombre: 'Adhesivos',
      descripcion: 'Pegamentos y cintas',
    });

    component.guardarCategoria();
    expect(component.modalAbierto()).toBe(false);
    expect(component.mensajeExito()).toContain('creada exitosamente');
  });
});
