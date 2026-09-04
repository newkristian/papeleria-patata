import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { TopNavBarComponent } from './top-nav-bar.component';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioSesion } from '../../core/models/usuario-sesion.model';
import { signal } from '@angular/core';


describe('TopNavBarComponent', () => {
  let currentUserSignal = signal<UsuarioSesion | null>(null);
  let logoutCalled = false;

  const authServiceMock = {
    currentUser: currentUserSignal,
    canAccessAdmin: () => true,
    logout: () => {
      logoutCalled = true;
    },
  };

  beforeEach(async () => {
    logoutCalled = false;
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
      imports: [TopNavBarComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should create the top nav bar and display user name and role', () => {
    const fixture = TestBed.createComponent(TopNavBarComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Administrador');
    expect(compiled.textContent).toContain('ADMINISTRADOR');
  });

  it('should toggle dropdown accordion menu on button click', () => {
    const fixture = TestBed.createComponent(TopNavBarComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.menuAbierto()).toBe(false);

    component.toggleMenu();
    expect(component.menuAbierto()).toBe(true);

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Mi Perfil (Editar datos y clave)');
    expect(compiled.textContent).toContain('Cerrar sesión');
  });

  it('should call authService.logout on logout click', () => {
    const fixture = TestBed.createComponent(TopNavBarComponent);
    const component = fixture.componentInstance;
    component.menuAbierto.set(true);
    fixture.detectChanges();

    component.onLogout();
    expect(logoutCalled).toBe(true);
    expect(component.menuAbierto()).toBe(false);
  });
});
