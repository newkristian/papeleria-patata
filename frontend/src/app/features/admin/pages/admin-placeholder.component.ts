import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-placeholder',
  imports: [RouterLink],
  template: `
    <div class="p-6 max-w-7xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-8 text-center">
        <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center">
          <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ titulo }}</h2>
        <p class="text-gray-600 max-w-md mx-auto mb-6">{{ descripcion }}</p>
        <div class="inline-flex items-center space-x-3">
          <a
            routerLink="/admin"
            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg font-medium text-sm transition-colors">
            Volver al Panel
          </a>
          <a
            routerLink="/pos"
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium text-sm transition-colors">
            Ir al POS
          </a>
        </div>
      </div>
    </div>
  `,
})
export class AdminPlaceholderComponent {
  private readonly route = inject(ActivatedRoute);

  get titulo(): string {
    return this.route.snapshot.data['titulo'] || 'Sección Administrativa';
  }

  get descripcion(): string {
    return this.route.snapshot.data['descripcion'] || 'Módulo administrativo en preparación.';
  }
}

