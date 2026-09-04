import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, of, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RolUsuario, UsuarioSesion } from '../models/usuario-sesion.model';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  requiereCambioPassword: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/api/v1/auth`;
  private readonly usuariosApiUrl = `${environment.apiUrl}/api/v1/usuarios`;
  private readonly ACCESS_TOKEN_KEY = 'pos_access_token';
  private readonly REFRESH_TOKEN_KEY = 'pos_refresh_token';
  private readonly USER_SESSION_KEY = 'pos_user_session';
  private readonly LEGACY_TOKEN_KEY = 'jwt_token';

  readonly isAuthenticated = signal(this.hasAccessToken());
  readonly currentUser = signal<UsuarioSesion | null>(this.getStoredUser());
  readonly userRole = computed<RolUsuario | null>(() => this.currentUser()?.rol ?? null);

  readonly isAdmin = computed(() => this.userRole() === 'ADMINISTRADOR');
  readonly isGerente = computed(() => this.userRole() === 'GERENTE');
  readonly isInventarista = computed(() => this.userRole() === 'INVENTARISTA');
  readonly isVendedor = computed(() => this.userRole() === 'VENDEDOR');

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginCredentials): Observable<UsuarioSesion> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (!response.accessToken || !response.refreshToken) {
          throw new Error('La respuesta de autenticación no contiene los tokens requeridos');
        }

        sessionStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
        localStorage.removeItem(this.LEGACY_TOKEN_KEY);
        this.isAuthenticated.set(true);
      }),
      switchMap(() => this.cargarPerfil())
    );
  }

  cargarPerfil(): Observable<UsuarioSesion> {
    return this.http.get<UsuarioSesion>(`${this.usuariosApiUrl}/perfil`).pipe(
      tap(perfil => {
        this.setStoredUser(perfil);
      }),
      catchError(err => {
        if (err?.status === 401) {
          this.logout();
        }
        return throwError(() => err);
      })
    );
  }

  hasRole(rol: RolUsuario): boolean {
    return this.userRole() === rol;
  }

  hasAnyRole(roles: RolUsuario[]): boolean {
    const rolActual = this.userRole();
    return rolActual !== null && roles.includes(rolActual);
  }

  canAccessAdmin(): boolean {
    return this.hasAnyRole(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA']);
  }

  canManageProducts(): boolean {
    return this.hasAnyRole(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA']);
  }

  canManageProviders(): boolean {
    return this.hasAnyRole(['ADMINISTRADOR', 'GERENTE']);
  }

  canDeactivateProviders(): boolean {
    return this.hasRole('ADMINISTRADOR');
  }

  canManageUsers(): boolean {
    return this.hasRole('ADMINISTRADOR');
  }

  canAccessPos(): boolean {
    return this.isAuthenticated();
  }

  logout(): void {
    sessionStorage.removeItem(this.ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(this.USER_SESSION_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.LEGACY_TOKEN_KEY);
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  private hasAccessToken(): boolean {
    return this.getAccessToken() !== null;
  }

  private getStoredUser(): UsuarioSesion | null {
    try {
      const data = sessionStorage.getItem(this.USER_SESSION_KEY);
      return data ? JSON.parse(data) as UsuarioSesion : null;
    } catch {
      return null;
    }
  }

  private setStoredUser(user: UsuarioSesion): void {
    this.currentUser.set(user);
    try {
      sessionStorage.setItem(this.USER_SESSION_KEY, JSON.stringify(user));
    } catch {
      // ignore storage quota errors
    }
  }
}

