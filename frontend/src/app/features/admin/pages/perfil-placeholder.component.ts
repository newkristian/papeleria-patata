import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { TopNavBarComponent } from '../../../shared/components/top-nav-bar.component';

@Component({
  selector: 'app-perfil-placeholder',
  imports: [TopNavBarComponent],
  template: `
    <div class="min-h-screen bg-gray-100 flex flex-col font-sans">
      <app-top-nav-bar />
      <main class="flex-1 p-6 max-w-4xl mx-auto w-full">
        <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 sm:p-8">
          <div class="flex items-center space-x-4 mb-6 pb-6 border-b border-gray-200">
            <div class="w-16 h-16 rounded-full bg-blue-600 text-white flex items-center justify-center text-2xl font-bold">
              {{ usuarioInicial() }}
            </div>
            <div>
              <h1 class="text-xl sm:text-2xl font-bold text-gray-900">{{ usuarioNombre() }}</h1>
              <p class="text-sm text-gray-500">{{ usuarioEmail() }}</p>
              <div class="mt-1 flex items-center space-x-2">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-blue-100 text-blue-800 uppercase">
                  {{ usuarioRol() }}
                </span>
                @if (tiendaNombre()) {
                  <span class="text-xs text-gray-600 font-medium">Sucursal: {{ tiendaNombre() }}</span>
                }
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 text-blue-800 text-sm">
              <strong>Perfil de Usuario:</strong> La edición completa de datos y cambio de contraseña estará disponible en los siguientes módulos de administración. Tu sesión se encuentra validada correctamente por el backend.
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="p-4 rounded-xl bg-gray-50 border border-gray-100">
                <span class="text-xs text-gray-500 font-medium">Nombre Completo</span>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ usuarioNombre() }}</p>
              </div>
              <div class="p-4 rounded-xl bg-gray-50 border border-gray-100">
                <span class="text-xs text-gray-500 font-medium">Correo Electrónico</span>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ usuarioEmail() }}</p>
              </div>
              <div class="p-4 rounded-xl bg-gray-50 border border-gray-100">
                <span class="text-xs text-gray-500 font-medium">Rol Asignado</span>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ usuarioRol() }}</p>
              </div>
              <div class="p-4 rounded-xl bg-gray-50 border border-gray-100">
                <span class="text-xs text-gray-500 font-medium">Sucursal Asignada</span>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ tiendaNombre() || 'Central / No asignada' }}</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  `,
})
export class PerfilPlaceholderComponent {
  readonly authService = inject(AuthService);

  readonly usuario = computed(() => this.authService.currentUser());
  readonly usuarioNombre = computed(() => this.usuario()?.nombre || this.usuario()?.username || 'Usuario');
  readonly usuarioRol = computed(() => this.usuario()?.rol || '');
  readonly usuarioEmail = computed(() => this.usuario()?.email || '');
  readonly tiendaNombre = computed(() => this.usuario()?.tiendaNombre || '');
  readonly usuarioInicial = computed(() => {
    const nombre = this.usuarioNombre();
    return nombre ? nombre.charAt(0).toUpperCase() : 'U';
  });
}
