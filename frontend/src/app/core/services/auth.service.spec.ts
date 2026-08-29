import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthResponse, AuthService, LoginCredentials } from './auth.service';

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

  it('should store the access token only in sessionStorage and the refresh token in localStorage', () => {
    const service = TestBed.inject(AuthService);

    service.login(credentials).subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne(
      'http://localhost:8080/api/v1/auth/login',
    );
    expect(request.request.body).toEqual(credentials);
    request.flush(authResponse);

    expect(sessionStorage.getItem('pos_access_token')).toBe(authResponse.accessToken);
    expect(localStorage.getItem('pos_access_token')).toBeNull();
    expect(localStorage.getItem('pos_refresh_token')).toBe(authResponse.refreshToken);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should restore the authenticated state from the session access token', () => {
    sessionStorage.setItem('pos_access_token', authResponse.accessToken);

    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.getAccessToken()).toBe(authResponse.accessToken);
  });

  it('should remove both tokens on logout', () => {
    sessionStorage.setItem('pos_access_token', authResponse.accessToken);
    localStorage.setItem('pos_refresh_token', authResponse.refreshToken);
    const service = TestBed.inject(AuthService);

    service.logout();

    expect(sessionStorage.getItem('pos_access_token')).toBeNull();
    expect(localStorage.getItem('pos_refresh_token')).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });
});
