import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  let authServiceMock: {
    isAuthenticated: () => boolean;
    hasAnyRole: (roles: string[]) => boolean;
  };

  let routerMock: {
    createUrlTree: (commands: any[]) => UrlTree;
  };

  const fakeUrlTree = {} as UrlTree;

  beforeEach(() => {
    authServiceMock = {
      isAuthenticated: () => false,
      hasAnyRole: () => false,
    };

    routerMock = {
      createUrlTree: () => fakeUrlTree,
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  it('should redirect to /login if user is not authenticated', () => {
    authServiceMock.isAuthenticated = () => false;
    let urlRequested: any[] | null = null;
    routerMock.createUrlTree = (commands: any[]) => {
      urlRequested = commands;
      return fakeUrlTree;
    };

    const guard = roleGuard(['ADMINISTRADOR']);
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBe(fakeUrlTree);
    expect(urlRequested).toEqual(['/login']);
  });

  it('should allow access if user is authenticated and has allowed role', () => {
    authServiceMock.isAuthenticated = () => true;
    authServiceMock.hasAnyRole = (roles: string[]) => roles.includes('ADMINISTRADOR');

    const guard = roleGuard(['ADMINISTRADOR', 'GERENTE']);
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBe(true);
  });

  it('should redirect to /pos if user is authenticated but does not have allowed role', () => {
    authServiceMock.isAuthenticated = () => true;
    authServiceMock.hasAnyRole = () => false;
    let urlRequested: any[] | null = null;
    routerMock.createUrlTree = (commands: any[]) => {
      urlRequested = commands;
      return fakeUrlTree;
    };

    const guard = roleGuard(['ADMINISTRADOR']);
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBe(fakeUrlTree);
    expect(urlRequested).toEqual(['/pos']);
  });
});
