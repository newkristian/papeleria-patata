import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TopNavBarComponent } from '../../../shared/components/top-nav-bar.component';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TopNavBarComponent],
  template: `
    <div class="min-h-screen bg-gray-100 flex flex-col font-sans">
      <!-- Barra superior transversal con nombre, rol, accesos y menú de usuario -->
      <app-top-nav-bar />

      <!-- Contenedor principal con Barra lateral y contenido -->
      <div class="flex-1 flex min-h-0">
        <!-- Barra lateral (Desktop / Tablet) -->
        <aside class="w-64 bg-white border-r border-gray-200 shrink-0 hidden md:flex flex-col justify-between p-4">
          <div class="space-y-1">
            <div class="px-3 py-2 text-2xs font-bold uppercase tracking-wider text-gray-400">
              Administración
            </div>

            <!-- Dashboard -->
            <a
              routerLink="/admin"
              [routerLinkActiveOptions]="{ exact: true }"
              routerLinkActive="bg-blue-50 text-blue-700 font-semibold"
              class="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors">
              <svg class="w-4 h-4 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              <span>Inicio Admin</span>
            </a>

            <!-- Productos -->
            @if (authService.canManageProducts()) {
              <a
                routerLink="/admin/productos"
                routerLinkActive="bg-blue-50 text-blue-700 font-semibold"
                class="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors">
                <svg class="w-4 h-4 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                <span>Productos y Fotos</span>
              </a>
            }

            <!-- Categorías -->
            @if (authService.canManageProducts()) {
              <a
                routerLink="/admin/categorias"
                routerLinkActive="bg-blue-50 text-blue-700 font-semibold"
                class="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors">
                <svg class="w-4 h-4 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
                <span>Categorías</span>
              </a>
            }

            <!-- Proveedores -->
            @if (authService.canManageProviders()) {
              <a
                routerLink="/admin/proveedores"
                routerLinkActive="bg-blue-50 text-blue-700 font-semibold"
                class="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors">
                <svg class="w-4 h-4 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
                <span>Proveedores</span>
              </a>
            }

            <!-- Inventario -->
            @if (authService.canManageProducts()) {
              <a
                routerLink="/admin/inventario"
                routerLinkActive="bg-blue-50 text-blue-700 font-semibold"
                class="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors">
                <svg class="w-4 h-4 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                </svg>
                <span>Inventario</span>
              </a>
            }

            <!-- Usuarios (Solo Administrador) -->
            @if (authService.canManageUsers()) {
              <div class="pt-4 px-3 py-1 text-2xs font-bold uppercase tracking-wider text-gray-400">
                Seguridad
              </div>
              <a
                routerLink="/admin/usuarios"
                routerLinkActive="bg-blue-50 text-blue-700 font-semibold"
                class="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors">
                <svg class="w-4 h-4 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                <span>Usuarios y Roles</span>
              </a>
            }
          </div>

          <!-- Pie lateral con retorno al POS -->
          <div class="border-t border-gray-200 pt-4 mt-6">
            <a
              routerLink="/pos"
              class="flex items-center justify-center w-full px-4 py-2.5 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-xl font-medium text-sm transition-colors shadow-sm">
              <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
              <span>Abrir POS</span>
            </a>
          </div>
        </aside>

        <!-- Barra inferior móvil/tablet de acceso rápido a administración -->
        <div class="md:hidden bg-white border-b border-gray-200 px-4 py-2 flex items-center overflow-x-auto space-x-2 text-xs">
          <a
            routerLink="/admin"
            [routerLinkActiveOptions]="{ exact: true }"
            routerLinkActive="bg-blue-100 text-blue-800 font-semibold"
            class="px-2.5 py-1.5 rounded-md text-gray-600 shrink-0">
            Inicio
          </a>
          @if (authService.canManageProducts()) {
            <a
              routerLink="/admin/productos"
              routerLinkActive="bg-blue-100 text-blue-800 font-semibold"
              class="px-2.5 py-1.5 rounded-md text-gray-600 shrink-0">
              Productos
            </a>
            <a
              routerLink="/admin/categorias"
              routerLinkActive="bg-blue-100 text-blue-800 font-semibold"
              class="px-2.5 py-1.5 rounded-md text-gray-600 shrink-0">
              Categorías
            </a>
          }
          @if (authService.canManageProviders()) {
            <a
              routerLink="/admin/proveedores"
              routerLinkActive="bg-blue-100 text-blue-800 font-semibold"
              class="px-2.5 py-1.5 rounded-md text-gray-600 shrink-0">
              Proveedores
            </a>
          }
          @if (authService.canManageProducts()) {
            <a
              routerLink="/admin/inventario"
              routerLinkActive="bg-blue-100 text-blue-800 font-semibold"
              class="px-2.5 py-1.5 rounded-md text-gray-600 shrink-0">
              Inventario
            </a>
          }
          @if (authService.canManageUsers()) {
            <a
              routerLink="/admin/usuarios"
              routerLinkActive="bg-blue-100 text-blue-800 font-semibold"
              class="px-2.5 py-1.5 rounded-md text-gray-600 shrink-0">
              Usuarios
            </a>
          }
        </div>

        <!-- Área de contenido de sub-rutas administrativas -->
        <main class="flex-1 overflow-y-auto min-h-0">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class AdminLayoutComponent {
  readonly authService = inject(AuthService);
}
