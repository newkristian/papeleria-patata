import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriaService } from '../../../core/services/categoria.service';
import { AuthService } from '../../../core/services/auth.service';
import { Categoria } from '../../../core/models/categoria.model';

@Component({
  selector: 'app-categorias-admin',
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="p-6 max-w-7xl mx-auto space-y-6">
      <!-- Encabezado de la Sección -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Gestión de Categorías</h1>
          <p class="text-sm text-gray-500 mt-1">
            Administra las categorías de productos para la clasificación del catálogo y punto de venta.
          </p>
        </div>
        @if (authService.canManageProducts()) {
          <button
            type="button"
            (click)="abrirModalCrear()"
            class="inline-flex items-center justify-center px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors">
            <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nueva Categoría
          </button>
        }
      </div>

      <!-- Alertas de Estado -->
      @if (mensajeExito()) {
        <div class="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <svg class="w-5 h-5 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <span>{{ mensajeExito() }}</span>
          </div>
          <button type="button" (click)="mensajeExito.set(null)" class="text-emerald-600 hover:text-emerald-800 font-bold">&times;</button>
        </div>
      }

      @if (mensajeError()) {
        <div class="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <svg class="w-5 h-5 text-rose-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ mensajeError() }}</span>
          </div>
          <button type="button" (click)="mensajeError.set(null)" class="text-rose-600 hover:text-rose-800 font-bold">&times;</button>
        </div>
      }

      <!-- Tabla / Lista de Categorías -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        @if (cargando()) {
          <div class="p-12 text-center text-gray-500">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-3"></div>
            <p class="text-sm">Cargando categorías...</p>
          </div>
        } @else if (categorias().length === 0) {
          <div class="p-12 text-center">
            <div class="w-12 h-12 rounded-full bg-gray-100 text-gray-400 mx-auto flex items-center justify-center mb-3">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
            </div>
            <h3 class="text-base font-bold text-gray-900">No hay categorías registradas</h3>
            <p class="text-sm text-gray-500 mt-1">Crea la primera categoría para organizar los productos de la papelería.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="border-b border-gray-200 bg-gray-50/75 text-xs font-bold uppercase tracking-wider text-gray-500">
                  <th class="py-3.5 px-6">ID</th>
                  <th class="py-3.5 px-6">Nombre</th>
                  <th class="py-3.5 px-6">Descripción</th>
                  <th class="py-3.5 px-6 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100 text-sm text-gray-700">
                @for (cat of categorias(); track cat.id) {
                  <tr class="hover:bg-gray-50/50 transition-colors">
                    <td class="py-3.5 px-6 font-mono text-xs text-gray-500">#{{ cat.id }}</td>
                    <td class="py-3.5 px-6 font-semibold text-gray-900">{{ cat.nombre }}</td>
                    <td class="py-3.5 px-6 text-gray-500 max-w-xs truncate">{{ cat.descripcion || 'Sin descripción' }}</td>
                    <td class="py-3.5 px-6 text-right space-x-2">
                      @if (authService.canManageProducts()) {
                        <button
                          type="button"
                          (click)="abrirModalEditar(cat)"
                          class="inline-flex items-center px-2.5 py-1.5 text-xs font-medium rounded-lg text-blue-700 bg-blue-50 hover:bg-blue-100 transition-colors">
                          Editar
                        </button>
                        <button
                          type="button"
                          (click)="confirmarEliminar(cat)"
                          class="inline-flex items-center px-2.5 py-1.5 text-xs font-medium rounded-lg text-rose-700 bg-rose-50 hover:bg-rose-100 transition-colors">
                          Eliminar
                        </button>
                      } @else {
                        <span class="text-xs text-gray-400 italic">Solo lectura</span>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <!-- Modal de Creación / Edición -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-md p-6 space-y-5 animate-in fade-in zoom-in-95">
            <div class="flex items-center justify-between border-b border-gray-100 pb-3">
              <h3 class="text-lg font-bold text-gray-900">
                {{ categoriaEnEdicion() ? 'Editar Categoría' : 'Nueva Categoría' }}
              </h3>
              <button
                type="button"
                (click)="cerrarModal()"
                class="text-gray-400 hover:text-gray-600 text-xl font-bold leading-none">
                &times;
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardarCategoria()" class="space-y-4">
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                  Nombre de la categoría *
                </label>
                <input
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Cuadernos y Libretas"
                  class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" />
                @if (form.get('nombre')?.touched && form.get('nombre')?.invalid) {
                  <p class="text-xs text-rose-600 mt-1">El nombre es obligatorio (máximo 255 caracteres).</p>
                }
              </div>

              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                  Descripción
                </label>
                <textarea
                  formControlName="descripcion"
                  rows="3"
                  placeholder="Detalles sobre los artículos de esta categoría..."
                  class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"></textarea>
              </div>

              <div class="flex items-center justify-end space-x-3 pt-2">
                <button
                  type="button"
                  (click)="cerrarModal()"
                  class="px-4 py-2 border border-gray-300 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">
                  Cancelar
                </button>
                <button
                  type="submit"
                  [disabled]="form.invalid || guardando()"
                  class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors">
                  {{ guardando() ? 'Guardando...' : (categoriaEnEdicion() ? 'Actualizar' : 'Crear') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
})
export class CategoriasAdminComponent implements OnInit {
  readonly categoriaService = inject(CategoriaService);
  readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly categorias = signal<Categoria[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly modalAbierto = signal(false);
  readonly categoriaEnEdicion = signal<Categoria | null>(null);

  readonly mensajeExito = signal<string | null>(null);
  readonly mensajeError = signal<string | null>(null);

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(255)]],
    descripcion: ['', [Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    this.cargarCategorias();
  }

  cargarCategorias(): void {
    this.cargando.set(true);
    this.categoriaService.listarTodas().subscribe({
      next: (data) => {
        this.categorias.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.mensajeError.set(err?.error?.mensaje || 'Error al cargar las categorías');
        this.cargando.set(false);
      },
    });
  }

  abrirModalCrear(): void {
    this.categoriaEnEdicion.set(null);
    this.form.reset();
    this.modalAbierto.set(true);
  }

  abrirModalEditar(categoria: Categoria): void {
    this.categoriaEnEdicion.set(categoria);
    this.form.patchValue({
      nombre: categoria.nombre,
      descripcion: categoria.descripcion || '',
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.categoriaEnEdicion.set(null);
    this.form.reset();
  }

  guardarCategoria(): void {
    if (this.form.invalid) return;

    this.guardando.set(true);
    this.mensajeError.set(null);

    const datos = {
      nombre: this.form.value.nombre!.trim(),
      descripcion: this.form.value.descripcion ? this.form.value.descripcion.trim() : null,
    };

    const edicion = this.categoriaEnEdicion();
    if (edicion) {
      this.categoriaService.actualizar(edicion.id, datos).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.mensajeExito.set('Categoría actualizada con éxito');
          this.cargarCategorias();
        },
        error: (err) => {
          this.guardando.set(false);
          this.mensajeError.set(err?.error?.mensaje || 'No se pudo actualizar la categoría');
        },
      });
    } else {
      this.categoriaService.crear(datos).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.mensajeExito.set('Categoría creada exitosamente');
          this.cargarCategorias();
        },
        error: (err) => {
          this.guardando.set(false);
          this.mensajeError.set(err?.error?.mensaje || 'No se pudo crear la categoría');
        },
      });
    }
  }

  confirmarEliminar(categoria: Categoria): void {
    if (confirm(`¿Estás seguro de que deseas eliminar la categoría "${categoria.nombre}"?`)) {
      this.categoriaService.eliminar(categoria.id).subscribe({
        next: () => {
          this.mensajeExito.set('Categoría eliminada exitosamente');
          this.cargarCategorias();
        },
        error: (err) => {
          this.mensajeError.set(err?.error?.mensaje || 'No se puede eliminar la categoría porque tiene productos asociados');
        },
      });
    }
  }
}
