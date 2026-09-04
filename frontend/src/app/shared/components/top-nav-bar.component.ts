import { Component, ElementRef, HostListener, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';


@Component({
  selector: 'app-top-nav-bar',
  imports: [RouterLink],
  template: `
    <header class="bg-white border-b border-gray-200 shadow-sm shrink-0 sticky top-0 z-40">
      <div class="px-4 sm:px-6 h-16 flex items-center justify-between">
        <!-- Logo / Marca y accesos rápidos -->
        <div class="flex items-center space-x-3 sm:space-x-6">
          <a routerLink="/" class="flex items-center space-x-2 text-blue-600 hover:text-blue-700 font-bold text-lg sm:text-xl tracking-tight">
            <span>Papelería Patata</span>
          </a>

          <!-- Enlaces de cambio de contexto -->
          <nav class="flex items-center space-x-2 text-sm">
            <a
              routerLink="/pos"
              class="px-3 py-1.5 rounded-md font-medium transition-colors text-gray-600 hover:text-blue-600 hover:bg-blue-50"
              [class.bg-blue-100]="isPosActive()"
              [class.text-blue-700]="isPosActive()">
              POS
            </a>

            @if (authService.canAccessAdmin()) {
              <a
                routerLink="/admin"
                class="px-3 py-1.5 rounded-md font-medium transition-colors text-gray-600 hover:text-blue-600 hover:bg-blue-50"
                [class.bg-blue-100]="isAdminActive()"
                [class.text-blue-700]="isAdminActive()">
                Administración
              </a>
            }
          </nav>
        </div>

        <!-- Información de usuario y Menú desplegable -->
        <div class="relative flex items-center space-x-3">
          <!-- Datos de usuario en barra (oculto en pantallas muy angostas) -->
          <div class="hidden md:flex flex-col text-right">
            <span class="text-sm font-semibold text-gray-800 leading-tight">
              {{ usuarioNombre() }}
            </span>
            <div class="flex items-center justify-end space-x-1.5 text-xs text-gray-500">
              <span class="inline-flex items-center px-1.5 py-0.5 rounded text-2xs font-semibold bg-gray-100 text-gray-700 uppercase tracking-wide">
                {{ usuarioRol() }}
              </span>
              @if (tiendaNombre()) {
                <span>· {{ tiendaNombre() }}</span>
              }
            </div>
          </div>

          <!-- Botón de Menú de Usuario -->
          <button
            type="button"
            (click)="toggleMenu()"
            [attr.aria-expanded]="menuAbierto()"
            aria-haspopup="true"
            class="flex items-center space-x-2 p-1.5 sm:px-3 sm:py-2 rounded-lg border border-gray-200 bg-gray-50 hover:bg-gray-100 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1 transition-colors">
            <div class="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-sm shadow-inner">
              {{ usuarioInicial() }}
            </div>
            <svg class="w-4 h-4 text-gray-500 transition-transform duration-200" [class.rotate-180]="menuAbierto()" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          <!-- Menú acordeón / dropdown desplegable -->
          @if (menuAbierto()) {
            <div
              class="absolute right-0 top-full mt-2 w-64 rounded-xl shadow-lg bg-white border border-gray-200 ring-1 ring-black/5 divide-y divide-gray-100 py-1 z-50 animate-in fade-in zoom-in-95 duration-100">
              <!-- Encabezado móvil del menú con info de usuario -->
              <div class="px-4 py-3 bg-gray-50/70 rounded-t-xl">
                <p class="text-xs font-medium text-gray-500">Sesión activa</p>
                <p class="text-sm font-bold text-gray-900 truncate">{{ usuarioNombre() }}</p>
                <p class="text-xs text-gray-500 truncate">{{ usuarioEmail() }}</p>
                <div class="mt-1.5 flex items-center space-x-1.5">
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">
                    {{ usuarioRol() }}
                  </span>
                  @if (tiendaNombre()) {
                    <span class="text-xs text-gray-600 font-medium">Sucursal: {{ tiendaNombre() }}</span>
                  }
                </div>
              </div>

              <!-- Opciones de Navegación -->
              <div class="py-1">
                <a
                  routerLink="/perfil"
                  (click)="cerrarMenu()"
                  class="flex items-center px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 hover:text-blue-600 transition-colors">
                  <svg class="w-4 h-4 mr-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>Mi Perfil (Editar datos y clave)</span>
                </a>

                <a
                  routerLink="/pos"
                  (click)="cerrarMenu()"
                  class="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 hover:text-blue-600 transition-colors">
                  <svg class="w-4 h-4 mr-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                  </svg>
                  <span>Punto de Venta (POS)</span>
                </a>

                @if (authService.canAccessAdmin()) {
                  <a
                    routerLink="/admin"
                    (click)="cerrarMenu()"
                    class="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 hover:text-blue-600 transition-colors">
                    <svg class="w-4 h-4 mr-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <span>Panel de Administración</span>
                  </a>
                }
              </div>

              <!-- Cerrar sesión -->
              <div class="py-1">
                <button
                  type="button"
                  (click)="onLogout()"
                  class="w-full text-left flex items-center px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors font-medium">
                  <svg class="w-4 h-4 mr-3 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  <span>Cerrar sesión</span>
                </button>
              </div>
            </div>
          }
        </div>
      </div>
    </header>
  `,
})
export class TopNavBarComponent {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);

  readonly menuAbierto = signal(false);

  readonly usuario = computed(() => this.authService.currentUser());
  readonly usuarioNombre = computed(() => this.usuario()?.nombre || this.usuario()?.username || 'Usuario');
  readonly usuarioRol = computed(() => this.usuario()?.rol || 'VENDEDOR');
  readonly usuarioEmail = computed(() => this.usuario()?.email || '');
  readonly tiendaNombre = computed(() => this.usuario()?.tiendaNombre || '');
  readonly usuarioInicial = computed(() => {
    const nombre = this.usuarioNombre();
    return nombre ? nombre.charAt(0).toUpperCase() : 'U';
  });

  isPosActive(): boolean {
    return this.router.url.startsWith('/pos');
  }

  isAdminActive(): boolean {
    return this.router.url.startsWith('/admin');
  }

  toggleMenu(): void {
    this.menuAbierto.update(v => !v);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  onLogout(): void {
    this.cerrarMenu();
    this.authService.logout();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.cerrarMenu();
    }
  }
}
