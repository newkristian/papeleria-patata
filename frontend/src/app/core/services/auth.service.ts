import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

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
  private readonly ACCESS_TOKEN_KEY = 'pos_access_token';
  private readonly REFRESH_TOKEN_KEY = 'pos_refresh_token';
  private readonly LEGACY_TOKEN_KEY = 'jwt_token';

  readonly isAuthenticated = signal(this.hasAccessToken());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (!response.accessToken || !response.refreshToken) {
          throw new Error('La respuesta de autenticación no contiene los tokens requeridos');
        }

        sessionStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
        localStorage.removeItem(this.LEGACY_TOKEN_KEY);
        this.isAuthenticated.set(true);
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.LEGACY_TOKEN_KEY);
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
}
