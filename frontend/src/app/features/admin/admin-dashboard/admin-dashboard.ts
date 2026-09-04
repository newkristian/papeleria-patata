import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink],
  template: `
    <div class="p-6 max-w-7xl mx-auto space-y-6">
      <!-- Tarjeta de Bienvenida -->
      <div class="bg-gradient-to-r from-blue-600 to-indigo-700 rounded-2xl shadow-sm p-6 sm:p-8 text-white">
        <div class="max-w-3xl">
          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white/20 text-white backdrop-blur-sm mb-3">
            Panel de Administración · {{ usuarioRol() }}
          </span>
          <h1 class="text-2xl sm:text-3xl font-extrabold tracking-tight mb-2">
            ¡Hola, {{ usuarioNombre() }}!
          </h1>
          <p class="text-blue-100 text-sm sm:text-base leading-relaxed">
            Bienvenido al módulo de gestión y control de Papelería Patata. Desde aquí podrás administrar el catálogo de productos, categorías, proveedores y el inventario de la tienda.
          </p>
        </div>
      </div>

      <!-- Tarjetas de Módulos según Rol -->
      <div>
        <h2 class="text-lg font-bold text-gray-900 mb-4">Módulos Administrativos</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          <!-- Productos / Catálogo -->
          @if (authService.canManageProducts()) {
            <a
              routerLink="/admin/productos"
              class="group block bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-blue-300 transition-all">
              <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
              </div>
              <h3 class="text-base font-bold text-gray-900 group-hover:text-blue-600 mb-1 transition-colors">
                Productos y Fotografías
              </h3>
              <p class="text-xs text-gray-500 leading-relaxed">
                Gestión completa de productos, alta, catálogo, código de barras y pipeline de fotografías.
              </p>
            </a>
          }

          <!-- Categorías -->
          @if (authService.canManageProducts()) {
            <a
              routerLink="/admin/categorias"
              class="group block bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-emerald-300 transition-all">
              <div class="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
              </div>
              <h3 class="text-base font-bold text-gray-900 group-hover:text-emerald-600 mb-1 transition-colors">
                Categorías
              </h3>
              <p class="text-xs text-gray-500 leading-relaxed">
                Clasificación de catálogo y organización temática para búsqueda rápida.
              </p>
            </a>
          }

          <!-- Proveedores -->
          @if (authService.canManageProviders()) {
            <a
              routerLink="/admin/proveedores"
              class="group block bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-purple-300 transition-all">
              <div class="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <h3 class="text-base font-bold text-gray-900 group-hover:text-purple-600 mb-1 transition-colors">
                Proveedores
              </h3>
              <p class="text-xs text-gray-500 leading-relaxed">
                Catálogo de proveedores, comisiones, cuentas y estado de proveedor pendiente.
              </p>
            </a>
          }

          <!-- Inventario -->
          @if (authService.canManageProducts()) {
            <a
              routerLink="/admin/inventario"
              class="group block bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-amber-300 transition-all">
              <div class="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                </svg>
              </div>
              <h3 class="text-base font-bold text-gray-900 group-hover:text-amber-600 mb-1 transition-colors">
                Control de Inventario
              </h3>
              <p class="text-xs text-gray-500 leading-relaxed">
                Entradas, salidas, ajustes absolutos de cantidad desconocida y existencias.
              </p>
            </a>
          }

          <!-- Usuarios (Solo Administrador) -->
          @if (authService.canManageUsers()) {
            <a
              routerLink="/admin/usuarios"
              class="group block bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-rose-300 transition-all">
              <div class="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <h3 class="text-base font-bold text-gray-900 group-hover:text-rose-600 mb-1 transition-colors">
                Gestión de Usuarios
              </h3>
              <p class="text-xs text-gray-500 leading-relaxed">
                Control de cuentas, roles (Administrador, Gerente, Inventarista, Vendedor) y contraseñas.
              </p>
            </a>
          }

          <!-- Acceso al POS -->
          <a
            routerLink="/pos"
            class="group block bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-indigo-300 transition-all">
            <div class="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
            </div>
            <h3 class="text-base font-bold text-gray-900 group-hover:text-indigo-600 mb-1 transition-colors">
              Ir a Punto de Venta
            </h3>
            <p class="text-xs text-gray-500 leading-relaxed">
              Abrir la terminal de caja y cobro para registrar transacciones y emitir tickets.
            </p>
          </a>
        </div>
      </div>
    </div>
  `,
})
export class AdminDashboardComponent {
  readonly authService = inject(AuthService);

  readonly usuario = computed(() => this.authService.currentUser());
  readonly usuarioNombre = computed(() => this.usuario()?.nombre || this.usuario()?.username || 'Usuario');
  readonly usuarioRol = computed(() => this.usuario()?.rol || '');
}
