import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout';
import { AuthService } from '../../../core/services/auth.service';
import { UsuarioSesion } from '../../../core/models/usuario-sesion.model';
import { signal } from '@angular/core';

describe('AdminLayoutComponent', () => {
  let currentUserSignal = signal<UsuarioSesion | null>(null);

  const authServiceMock = {
    currentUser: currentUserSignal,
    canAccessAdmin: () => true,
    canManageProducts: () => true,
    canManageProviders: () => true,
    canManageUsers: () => true,
    logout: () => {},
  };

  beforeEach(async () => {
    currentUserSignal.set({
      id: 1,
      username: 'admin@pos.com',
      nombre: 'Administrador',
      apellidos: 'Sistema',
      email: 'admin@pos.com',
      rol: 'ADMINISTRADOR',
      tiendaId: 1,
      tiendaNombre: 'Sucursal Centro',
    });

    await TestBed.configureTestingModule({
      imports: [AdminLayoutComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should create the admin layout', () => {
    const fixture = TestBed.createComponent(AdminLayoutComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should render top navigation and sidebar navigation', () => {
    const fixture = TestBed.createComponent(AdminLayoutComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('app-top-nav-bar')).not.toBeNull();
    expect(compiled.querySelector('aside')).not.toBeNull();
  });
});
