import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthResponse, AuthService, LoginCredentials } from './auth.service';
import { UsuarioSesion } from '../models/usuario-sesion.model';

describe('AuthService', () => {
  const credentials: LoginCredentials = {
    username: 'caja@pos.com',
    password: 'caja123',
  };

  const authResponse: AuthResponse = {
    accessToken: 'access-token',
    refreshToken: 'refresh-token',
    requiereCambioPassword: false,
  };

  const usuarioPerfil: UsuarioSesion = {
    id: 1,
    username: 'caja@pos.com',
    nombre: 'Cajero',
    apellidos: 'Principal',
    email: 'caja@pos.com',
    rol: 'VENDEDOR',
    tiendaId: 1,
    tiendaNombre: 'Matriz',
  };

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('should store the access token in sessionStorage and load user profile on login', () => {
    const service = TestBed.inject(AuthService);

    service.login(credentials).subscribe(user => {
      expect(user).toEqual(usuarioPerfil);
    });

    const loginReq = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/auth/login'
    );
    expect(loginReq.request.body).toEqual(credentials);
    loginReq.flush(authResponse);

    const perfilReq = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/usuarios/perfil'
    );
    perfilReq.flush(usuarioPerfil);

    expect(sessionStorage.getItem('pos_access_token')).toBe(authResponse.accessToken);
    expect(localStorage.getItem('pos_access_token')).toBeNull();
    expect(localStorage.getItem('pos_refresh_token')).toBe(authResponse.refreshToken);
    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()).toEqual(usuarioPerfil);
    expect(service.userRole()).toBe('VENDEDOR');
    expect(service.isVendedor()).toBe(true);
    expect(service.isAdmin()).toBe(false);
    expect(service.canAccessAdmin()).toBe(false);
    expect(service.canAccessPos()).toBe(true);
  });

  it('should restore the authenticated state and user from session storage', () => {
    sessionStorage.setItem('pos_access_token', authResponse.accessToken);
    sessionStorage.setItem('pos_user_session', JSON.stringify(usuarioPerfil));

    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.getAccessToken()).toBe(authResponse.accessToken);
    expect(service.currentUser()).toEqual(usuarioPerfil);
    expect(service.userRole()).toBe('VENDEDOR');
  });

  it('should remove both tokens and user on logout', () => {
    sessionStorage.setItem('pos_access_token', authResponse.accessToken);
    sessionStorage.setItem('pos_user_session', JSON.stringify(usuarioPerfil));
    localStorage.setItem('pos_refresh_token', authResponse.refreshToken);

    const service = TestBed.inject(AuthService);

    service.logout();

    expect(sessionStorage.getItem('pos_access_token')).toBeNull();
    expect(sessionStorage.getItem('pos_user_session')).toBeNull();
    expect(localStorage.getItem('pos_refresh_token')).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('should verify roles correctly for ADMINISTRADOR, GERENTE and INVENTARISTA', () => {
    const service = TestBed.inject(AuthService);

    const adminUser: UsuarioSesion = { ...usuarioPerfil, rol: 'ADMINISTRADOR' };
    (service.currentUser as any).set(adminUser);

    expect(service.isAdmin()).toBe(true);
    expect(service.canAccessAdmin()).toBe(true);
    expect(service.canManageProducts()).toBe(true);
    expect(service.canManageProviders()).toBe(true);
    expect(service.canDeactivateProviders()).toBe(true);
    expect(service.canManageUsers()).toBe(true);

    const inventaristaUser: UsuarioSesion = { ...usuarioPerfil, rol: 'INVENTARISTA' };
    (service.currentUser as any).set(inventaristaUser);

    expect(service.isInventarista()).toBe(true);
    expect(service.canAccessAdmin()).toBe(true);
    expect(service.canManageProducts()).toBe(true);
    expect(service.canManageProviders()).toBe(false);
    expect(service.canDeactivateProviders()).toBe(false);
    expect(service.canManageUsers()).toBe(false);
  });
});
