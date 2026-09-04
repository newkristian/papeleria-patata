import { HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';

describe('errorInterceptor', () => {
  let logoutCalled: boolean;
  const authServiceMock = {
    logout: () => {
      logoutCalled = true;
    },
  };

  beforeEach(() => {
    logoutCalled = false;
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
      ],
    });
  });

  it('should call authService.logout on 401 Unauthorized for non-login requests', async () => {
    const req = new HttpRequest('GET', '/api/v1/productos');
    const next: HttpHandlerFn = () =>
      throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }));

    try {
      await TestBed.runInInjectionContext(() => firstValueFrom(errorInterceptor(req, next)));
      expect.unreachable('Should have thrown 401');
    } catch (err: any) {
      expect(logoutCalled).toBe(true);
      expect(err.status).toBe(401);
    }
  });

  it('should NOT call authService.logout on 401 for login request', async () => {
    const req = new HttpRequest('POST', '/api/v1/auth/login', {});
    const next: HttpHandlerFn = () =>
      throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }));

    try {
      await TestBed.runInInjectionContext(() => firstValueFrom(errorInterceptor(req, next)));
      expect.unreachable('Should have thrown 401');
    } catch (err: any) {
      expect(logoutCalled).toBe(false);
      expect(err.status).toBe(401);
    }
  });

  it('should pass through successful requests untouched', async () => {
    const req = new HttpRequest('GET', '/api/v1/productos');
    const next: HttpHandlerFn = () => of({ ok: true } as any);

    const res = await TestBed.runInInjectionContext(() => firstValueFrom(errorInterceptor(req, next)));
    expect(logoutCalled).toBe(false);
    expect((res as any).ok).toBe(true);
  });
});
