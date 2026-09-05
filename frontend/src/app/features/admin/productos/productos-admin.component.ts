import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  Pagina,
  ProductoActualizarRequest,
  ProductoCrearRequest,
  ProductoDetalle,
  ProductoFiltros,
  ProductoListado,
} from '../../../core/models/producto.model';
import { Categoria } from '../../../core/models/categoria.model';
import { Proveedor } from '../../../core/models/proveedor.model';
import { ProductoFotosModalComponent } from './producto-fotos-modal.component';

@Component({
  selector: 'app-productos-admin',
  imports: [CommonModule, ReactiveFormsModule, ProductoFotosModalComponent],
  template: `
    <div class="p-6 max-w-7xl mx-auto space-y-6">
      <!-- Encabezado de la Sección -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Catálogo de Productos</h1>
          <p class="text-sm text-gray-500 mt-1">
            Administra el catálogo general, precios, códigos de barras, categorías y proveedores.
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
            Nuevo Producto
          </button>
        }
      </div>

      <!-- Barra de Búsqueda y Filtros Combinados -->
      <div class="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Búsqueda rápida por texto / código -->
          <div class="relative lg:col-span-2">
            <svg class="w-5 h-5 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              [value]="terminoBusqueda()"
              (input)="onTerminoChange($event)"
              placeholder="Buscar por nombre, código o descripción..."
              class="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" />
          </div>

          <!-- Selector de Categoría -->
          <div>
            <select
              [value]="categoriaFiltro() ?? ''"
              (change)="onCategoriaChange($event)"
              class="w-full px-3 py-2 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">Todas las categorías</option>
              @for (cat of categorias(); track cat.id) {
                <option [value]="cat.id">{{ cat.nombre }}</option>
              }
            </select>
          </div>

          <!-- Selector de Proveedor -->
          <div>
            <select
              [value]="proveedorFiltro() ?? ''"
              (change)="onProveedorChange($event)"
              class="w-full px-3 py-2 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">Todos los proveedores</option>
              @for (prov of proveedores(); track prov.id) {
                <option [value]="prov.id">{{ prov.nombre }}</option>
              }
            </select>
          </div>
        </div>

        <!-- Filtros secundarios: Estado y Condición de Inventario -->
        <div class="flex flex-wrap items-center gap-4 pt-2 border-t border-gray-100 text-sm text-gray-600">
          <div class="flex items-center space-x-2">
            <span class="text-xs font-bold uppercase tracking-wider text-gray-500">Estado:</span>
            <select
              [value]="activoFiltro() === null ? 'todos' : (activoFiltro() ? 'activos' : 'inactivos')"
              (change)="onActivoChange($event)"
              class="px-2.5 py-1.5 rounded-lg border border-gray-300 text-xs bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="activos">Solo activos</option>
              <option value="inactivos">Solo inactivos</option>
              <option value="todos">Todos los estados</option>
            </select>
          </div>

          <div class="flex items-center space-x-2">
            <span class="text-xs font-bold uppercase tracking-wider text-gray-500">Inventario:</span>
            <select
              [value]="condicionInventario()"
              (change)="onCondicionInventarioChange($event)"
              class="px-2.5 py-1.5 rounded-lg border border-gray-300 text-xs bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="todos">Todos los productos</option>
              <option value="stockBajo">Solo stock bajo</option>
              <option value="porContar">Cantidad desconocida (Por contar)</option>
            </select>
          </div>

          <button
            type="button"
            (click)="limpiarFiltros()"
            class="text-xs text-blue-600 hover:text-blue-800 font-medium ml-auto">
            Limpiar filtros
          </button>
        </div>
      </div>

      <!-- Alertas de Estado -->
      @if (mensajeExito()) {
        <div class="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <svg class="w-5 h-5 text-emerald-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
            <svg class="w-5 h-5 text-rose-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ mensajeError() }}</span>
          </div>
          <button type="button" (click)="mensajeError.set(null)" class="text-rose-600 hover:text-rose-800 font-bold">&times;</button>
        </div>
      }

      <!-- Tabla de Productos -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        @if (cargando()) {
          <div class="p-12 text-center text-gray-500">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-3"></div>
            <p class="text-sm">Cargando productos...</p>
          </div>
        } @else if (productos().length === 0) {
          <div class="p-12 text-center">
            <div class="w-12 h-12 rounded-full bg-gray-100 text-gray-400 mx-auto flex items-center justify-center mb-3">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <h3 class="text-base font-bold text-gray-900">No se encontraron productos</h3>
            <p class="text-sm text-gray-500 mt-1">Prueba ajustando los términos de búsqueda o filtros seleccionados.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="border-b border-gray-200 bg-gray-50/75 text-xs font-bold uppercase tracking-wider text-gray-500">
                  <th class="py-3.5 px-6">Producto</th>
                  <th class="py-3.5 px-6">Categoría / Proveedor</th>
                  <th class="py-3.5 px-6">Precio de Venta</th>
                  <th class="py-3.5 px-6">Existencia</th>
                  <th class="py-3.5 px-6">Estado</th>
                  <th class="py-3.5 px-6 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100 text-sm text-gray-700">
                @for (prod of productos(); track prod.id) {
                  <tr class="hover:bg-gray-50/50 transition-colors" [class.bg-gray-50]="!prod.activo">
                    <!-- Producto: Foto + Nombre + Código -->
                    <td class="py-3.5 px-6">
                      <div class="flex items-center space-x-3">
                        <div class="w-10 h-10 rounded-lg bg-gray-100 border border-gray-200 flex items-center justify-center overflow-hidden shrink-0">
                          @if (prod.urlThumbnail) {
                            <img [src]="prod.urlThumbnail" [alt]="prod.nombre" class="w-full h-full object-cover" />
                          } @else {
                            <svg class="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                          }
                        </div>
                        <div>
                          <div class="font-semibold text-gray-900 leading-snug">{{ prod.nombre }}</div>
                          <div class="text-xs font-mono text-gray-500 flex items-center space-x-1.5 mt-0.5">
                            <span>{{ prod.codigoBarras }}</span>
                          </div>
                        </div>
                      </div>
                    </td>

                    <!-- Categoría y Proveedor -->
                    <td class="py-3.5 px-6">
                      <div class="space-y-1">
                        <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
                          {{ prod.categoriaNombre }}
                        </span>
                        <div class="text-xs text-gray-500 flex items-center space-x-1">
                          @if (prod.proveedorNombre === 'PENDIENTE') {
                            <span class="inline-flex items-center px-1.5 py-0.2 rounded text-[10px] font-bold bg-amber-100 text-amber-800">
                              PENDIENTE
                            </span>
                          } @else {
                            <span class="truncate max-w-[160px]">{{ prod.proveedorNombre }}</span>
                          }
                        </div>
                      </div>
                    </td>

                    <!-- Precio de Venta -->
                    <td class="py-3.5 px-6">
                      <div class="font-bold text-gray-900">
                        {{ prod.precioVenta | currency:'USD':'symbol':'1.2-2' }}
                      </div>
                    </td>

                    <!-- Existencia / Stock -->
                    <td class="py-3.5 px-6">
                      @if (prod.cantidadDesconocida) {
                        <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                          <svg class="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                          Por contar
                        </span>
                      } @else {
                        <div class="flex items-center space-x-2">
                          <span class="font-semibold text-gray-900">{{ prod.stockActual }}</span>
                          @if (prod.stockActual <= prod.stockMinimo) {
                            <span class="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold bg-rose-100 text-rose-700">
                              Bajo (Mín. {{ prod.stockMinimo }})
                            </span>
                          }
                        </div>
                      }
                    </td>

                    <!-- Estado -->
                    <td class="py-3.5 px-6">
                      @if (prod.activo) {
                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                          Activo
                        </span>
                      } @else {
                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600 border border-gray-200">
                          Inactivo
                        </span>
                      }
                    </td>

                    <!-- Acciones -->
                    <td class="py-3.5 px-6 text-right">
                      <div class="inline-flex items-center space-x-2">
                        @if (authService.canManageProducts()) {
                          <button
                            type="button"
                            (click)="abrirModalFotos(prod)"
                            class="p-1.5 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                            title="Gestionar fotografías">
                            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                          </button>
                          <button
                            type="button"
                            (click)="abrirModalEditar(prod.id)"
                            class="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Editar producto">
                            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                          </button>
                        }

                        @if (authService.canDeactivateProviders()) {
                          @if (prod.activo) {
                            <button
                              type="button"
                              (click)="abrirModalEstado(prod, false)"
                              class="p-1.5 text-gray-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                              title="Desactivar producto">
                              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                              </svg>
                            </button>
                          } @else {
                            <button
                              type="button"
                              (click)="abrirModalEstado(prod, true)"
                              class="p-1.5 text-gray-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                              title="Reactivar producto">
                              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                              </svg>
                            </button>
                          }
                        }
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Paginador -->
          @if (totalPaginas() > 1) {
            <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between text-sm text-gray-600">
              <div>
                Mostrando página <span class="font-bold">{{ paginaActual() + 1 }}</span> de <span class="font-bold">{{ totalPaginas() }}</span>
                ({{ totalElementos() }} productos)
              </div>
              <div class="flex items-center space-x-2">
                <button
                  type="button"
                  [disabled]="paginaActual() === 0"
                  (click)="cambiarPagina(paginaActual() - 1)"
                  class="px-3 py-1.5 rounded-lg border border-gray-300 disabled:opacity-40 hover:bg-gray-50 transition-colors">
                  Anterior
                </button>
                <button
                  type="button"
                  [disabled]="paginaActual() >= totalPaginas() - 1"
                  (click)="cambiarPagina(paginaActual() + 1)"
                  class="px-3 py-1.5 rounded-lg border border-gray-300 disabled:opacity-40 hover:bg-gray-50 transition-colors">
                  Siguiente
                </button>
              </div>
            </div>
          }
        }
      </div>

      <!-- Modal de Creación / Edición de Producto -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-2xl p-6 space-y-5 animate-in fade-in zoom-in-95 my-8">
            <div class="flex items-center justify-between border-b border-gray-100 pb-3">
              <h3 class="text-lg font-bold text-gray-900">
                {{ productoEnEdicion() ? 'Editar Producto' : 'Nuevo Producto' }}
              </h3>
              <button
                type="button"
                (click)="cerrarModal()"
                class="text-gray-400 hover:text-gray-600 text-xl font-bold leading-none">
                &times;
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardarProducto()" class="space-y-4">
              <!-- Código y Nombre -->
              <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Código de Barras *
                  </label>
                  <input
                    type="text"
                    formControlName="codigoBarras"
                    placeholder="75010001"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm uppercase focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono" />
                  @if (form.get('codigoBarras')?.touched && form.get('codigoBarras')?.invalid) {
                    <p class="text-xs text-rose-600 mt-1">Obligatorio, sin espacios ni caracteres especiales.</p>
                  }
                </div>

                <div class="sm:col-span-2">
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Nombre del Producto *
                  </label>
                  <input
                    type="text"
                    formControlName="nombre"
                    placeholder="Ej. Cuaderno Profesional Raya 100h"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  @if (form.get('nombre')?.touched && form.get('nombre')?.invalid) {
                    <p class="text-xs text-rose-600 mt-1">Obligatorio (máx. 200 caracteres).</p>
                  }
                </div>
              </div>

              <!-- Categoría y Proveedor -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <div class="flex items-center justify-between mb-1">
                    <label class="block text-xs font-bold uppercase tracking-wider text-gray-600">
                      Categoría *
                    </label>
                    <button
                      type="button"
                      (click)="cargarCatalogosRelacionados()"
                      class="text-[11px] text-blue-600 hover:text-blue-800 font-medium">
                      Actualizar listas
                    </button>
                  </div>
                  <select
                    formControlName="categoriaId"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option [value]="null" disabled>Selecciona una categoría...</option>
                    @for (cat of categorias(); track cat.id) {
                      <option [value]="cat.id">{{ cat.nombre }}</option>
                    }
                  </select>
                  @if (form.get('categoriaId')?.touched && form.get('categoriaId')?.invalid) {
                    <p class="text-xs text-rose-600 mt-1">La categoría es obligatoria.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Proveedor (Opcional)
                  </label>
                  <select
                    formControlName="proveedorId"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option [value]="null">(Sin proveedor - Asignar PENDIENTE)</option>
                    @for (prov of proveedores(); track prov.id) {
                      @if (!prov.sistema) {
                        <option [value]="prov.id">{{ prov.nombre }}</option>
                      }
                    }
                  </select>
                  <p class="text-[11px] text-gray-500 mt-1">Si no se especifica, el sistema lo marcará como PENDIENTE.</p>
                </div>
              </div>

              <!-- Descripción -->
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                  Descripción (Opcional)
                </label>
                <textarea
                  rows="2"
                  formControlName="descripcion"
                  placeholder="Detalles adicionales, medidas, marca o especificaciones..."
                  class="w-full px-3.5 py-2 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
              </div>

              <!-- Costo, Margen, Unidad de Medida y Stock Mínimo -->
              <div class="grid grid-cols-1 sm:grid-cols-4 gap-3">
                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Costo Compra ($) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    formControlName="costoCompra"
                    placeholder="0.00"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  @if (form.get('costoCompra')?.touched && form.get('costoCompra')?.invalid) {
                    <p class="text-xs text-rose-600 mt-1">Mayor a 0.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Margen Manual (%)
                  </label>
                  @if (authService.canManageProviders()) {
                    <input
                      type="number"
                      step="0.01"
                      formControlName="porcentajeGananciaManual"
                      placeholder="Auto"
                      class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  } @else {
                    <input
                      type="text"
                      disabled
                      value="Calculado por sistema"
                      class="w-full px-3.5 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-xs text-gray-500 cursor-not-allowed" />
                  }
                </div>

                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Stock Mínimo *
                  </label>
                  <input
                    type="number"
                    formControlName="stockMinimo"
                    placeholder="5"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Unidad *
                  </label>
                  <select
                    formControlName="unidadMedida"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="PIEZA">PIEZA</option>
                    <option value="PAQUETE">PAQUETE</option>
                    <option value="CAJA">CAJA</option>
                    <option value="METRO">METRO</option>
                    <option value="KILO">KILO</option>
                  </select>
                </div>
              </div>

              <!-- Opción: Cantidad aún no contabilizada (solo para alta) -->
              @if (!productoEnEdicion()) {
                <div class="p-3.5 rounded-xl bg-amber-50 border border-amber-200 flex items-start space-x-3">
                  <input
                    type="checkbox"
                    id="chkCantidadDesconocida"
                    formControlName="cantidadDesconocida"
                    class="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                  <label for="chkCantidadDesconocida" class="text-xs text-amber-900 cursor-pointer">
                    <span class="font-bold">Cantidad aún no contabilizada</span>
                    <p class="text-amber-800/80 mt-0.5">
                      No solicita cantidad inicial. El producto iniciará en estado "Por contar" y no bloqueará la venta cuando se autorice su inventario.
                    </p>
                  </label>
                </div>
              }

              <!-- Nota de cálculo por Backend -->
              <div class="p-3 rounded-xl bg-blue-50 border border-blue-100 text-xs text-blue-800 flex items-center space-x-2">
                <svg class="w-4 h-4 text-blue-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>
                  El precio de venta final es calculado con precisión monetaria por el backend aplicando la escala de ganancia o el margen fijado.
                </span>
              </div>

              <!-- Botones del Modal -->
              <div class="flex items-center justify-end space-x-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  (click)="cerrarModal()"
                  class="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-xl transition-colors">
                  Cancelar
                </button>
                <button
                  type="submit"
                  [disabled]="form.invalid || guardando()"
                  class="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors flex items-center">
                  @if (guardando()) {
                    <div class="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  }
                  {{ productoEnEdicion() ? 'Guardar Cambios' : 'Registrar Producto' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }

      <!-- Modal de Confirmación de Desactivación / Reactivación -->
      @if (modalEstado()) {
        <div class="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-md p-6 space-y-4 animate-in fade-in zoom-in-95">
            <div class="flex items-center space-x-3 text-amber-600">
              <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center shrink-0">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <h3 class="text-lg font-bold text-gray-900">
                {{ accionEstado() ? 'Reactivar Producto' : 'Desactivar Producto' }}
              </h3>
            </div>

            <p class="text-sm text-gray-600">
              ¿Estás seguro de que deseas {{ accionEstado() ? 'reactivar' : 'desactivar' }} el producto
              <span class="font-bold text-gray-900">"{{ productoParaEstado()?.nombre }}"</span>?
              @if (!accionEstado()) {
                <span class="block mt-2 text-xs text-rose-600 font-medium">
                  El producto dejará de estar disponible en el Punto de Venta (POS) pero mantendrá intacto su historial de ventas y movimientos.
                </span>
              }
            </p>

            <div class="flex items-center justify-end space-x-3 pt-3 border-t border-gray-100">
              <button
                type="button"
                (click)="modalEstado.set(false)"
                class="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-xl transition-colors">
                Cancelar
              </button>
              <button
                type="button"
                [disabled]="guardando()"
                (click)="ejecutarCambioEstado()"
                [class.bg-rose-600]="!accionEstado()"
                [class.hover:bg-rose-700]="!accionEstado()"
                [class.bg-emerald-600]="accionEstado()"
                [class.hover:bg-emerald-700]="accionEstado()"
                class="px-4 py-2 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors">
                {{ guardando() ? 'Procesando...' : (accionEstado() ? 'Confirmar Reactivación' : 'Confirmar Desactivación') }}
              </button>
            </div>
          </div>
        </div>
      }

      <!-- Modal de Gestión de Fotografías -->
      @if (modalFotos() && productoParaFotos()) {
        <app-producto-fotos-modal
          [productoId]="productoParaFotos()!.id"
          [productoNombre]="productoParaFotos()!.nombre"
          (cerrar)="cerrarModalFotos()"
          (fotosActualizadas)="onFotosActualizadas()">
        </app-producto-fotos-modal>
      }
    </div>
  `,
})
export class ProductosAdminComponent implements OnInit {
  readonly productoService = inject(ProductoService);
  readonly categoriaService = inject(CategoriaService);
  readonly proveedorService = inject(ProveedorService);
  readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  // Estados de datos
  readonly productos = signal<ProductoListado[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly proveedores = signal<Proveedor[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);

  // Filtros
  readonly terminoBusqueda = signal('');
  readonly categoriaFiltro = signal<number | null>(null);
  readonly proveedorFiltro = signal<number | null>(null);
  readonly activoFiltro = signal<boolean | null>(true); // Activos por defecto
  readonly condicionInventario = signal<'todos' | 'stockBajo' | 'porContar'>('todos');

  // Paginación
  readonly paginaActual = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalElementos = signal(0);

  // Modales
  readonly modalAbierto = signal(false);
  readonly productoEnEdicion = signal<ProductoDetalle | null>(null);

  readonly modalEstado = signal(false);
  readonly productoParaEstado = signal<ProductoListado | null>(null);
  readonly accionEstado = signal<boolean>(false); // false = desactivar, true = reactivar

  readonly modalFotos = signal(false);
  readonly productoParaFotos = signal<ProductoListado | null>(null);

  // Notificaciones
  readonly mensajeExito = signal<string | null>(null);
  readonly mensajeError = signal<string | null>(null);

  readonly form = this.fb.group({
    codigoBarras: ['', [Validators.required, Validators.maxLength(50), Validators.pattern('^[A-Za-z0-9._-]+$')]],
    nombre: ['', [Validators.required, Validators.maxLength(200)]],
    descripcion: ['', [Validators.maxLength(500)]],
    categoriaId: [null as number | null, [Validators.required, Validators.min(1)]],
    proveedorId: [null as number | null],
    costoCompra: [null as number | null, [Validators.required, Validators.min(0.01)]],
    porcentajeGananciaManual: [null as number | null, [Validators.min(0), Validators.max(999.99)]],
    stockMinimo: [5, [Validators.required, Validators.min(0)]],
    unidadMedida: ['PIEZA', [Validators.required, Validators.maxLength(50)]],
    cantidadDesconocida: [true],
  });

  ngOnInit(): void {
    this.cargarCatalogosRelacionados();
    this.cargarProductos();
  }

  cargarCatalogosRelacionados(): void {
    this.categoriaService.listarTodas().subscribe({
      next: (cats) => this.categorias.set(cats),
      error: () => {},
    });
    this.proveedorService.listarTodos().subscribe({
      next: (provs) => this.proveedores.set(provs),
      error: () => {},
    });
  }

  cargarProductos(): void {
    this.cargando.set(true);

    const filtros: ProductoFiltros = {
      termino: this.terminoBusqueda().trim() || null,
      categoriaId: this.categoriaFiltro(),
      proveedorId: this.proveedorFiltro(),
      activo: this.activoFiltro(),
      soloStockBajo: this.condicionInventario() === 'stockBajo' ? true : null,
    };

    this.productoService.buscarAvanzado(filtros, this.paginaActual(), 15).subscribe({
      next: (pagina: Pagina<ProductoListado>) => {
        let items = pagina.content;
        // Filtrar condición local si es porContar (cantidad desconocida)
        if (this.condicionInventario() === 'porContar') {
          items = items.filter((p) => p.cantidadDesconocida);
        }
        this.productos.set(items);
        this.totalPaginas.set(pagina.page.totalPages);
        this.totalElementos.set(pagina.page.totalElements);
        this.cargando.set(false);
      },
      error: (err) => {
        this.mensajeError.set(err?.error?.mensaje || 'Error al consultar productos');
        this.cargando.set(false);
      },
    });
  }

  onTerminoChange(event: Event): void {
    const valor = (event.target as HTMLInputElement).value;
    this.terminoBusqueda.set(valor);
    this.paginaActual.set(0);
    this.cargarProductos();
  }

  onCategoriaChange(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value;
    this.categoriaFiltro.set(valor ? Number(valor) : null);
    this.paginaActual.set(0);
    this.cargarProductos();
  }

  onProveedorChange(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value;
    this.proveedorFiltro.set(valor ? Number(valor) : null);
    this.paginaActual.set(0);
    this.cargarProductos();
  }

  onActivoChange(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value;
    if (valor === 'activos') {
      this.activoFiltro.set(true);
    } else if (valor === 'inactivos') {
      this.activoFiltro.set(false);
    } else {
      this.activoFiltro.set(null);
    }
    this.paginaActual.set(0);
    this.cargarProductos();
  }

  onCondicionInventarioChange(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value as 'todos' | 'stockBajo' | 'porContar';
    this.condicionInventario.set(valor);
    this.paginaActual.set(0);
    this.cargarProductos();
  }

  limpiarFiltros(): void {
    this.terminoBusqueda.set('');
    this.categoriaFiltro.set(null);
    this.proveedorFiltro.set(null);
    this.activoFiltro.set(true);
    this.condicionInventario.set('todos');
    this.paginaActual.set(0);
    this.cargarProductos();
  }

  cambiarPagina(nuevaPagina: number): void {
    this.paginaActual.set(nuevaPagina);
    this.cargarProductos();
  }

  abrirModalCrear(): void {
    this.productoEnEdicion.set(null);
    this.cargarCatalogosRelacionados();
    this.form.reset({
      stockMinimo: 5,
      unidadMedida: 'PIEZA',
      cantidadDesconocida: true,
      categoriaId: null,
      proveedorId: null,
    });
    this.modalAbierto.set(true);
  }

  abrirModalEditar(id: number): void {
    this.cargando.set(true);
    this.cargarCatalogosRelacionados();
    this.productoService.obtenerPorId(id).subscribe({
      next: (producto) => {
        this.productoEnEdicion.set(producto);
        this.form.patchValue({
          codigoBarras: producto.codigoBarras,
          nombre: producto.nombre,
          descripcion: producto.descripcion || '',
          categoriaId: producto.categoria?.id ?? null,
          proveedorId: producto.proveedor?.sistema ? null : (producto.proveedor?.id ?? null),
          costoCompra: producto.costoCompra,
          porcentajeGananciaManual: producto.porcentajeGanancia,
          stockMinimo: producto.stockMinimo,
          unidadMedida: producto.unidadMedida,
          cantidadDesconocida: producto.cantidadDesconocida,
        });
        this.cargando.set(false);
        this.modalAbierto.set(true);
      },
      error: (err) => {
        this.cargando.set(false);
        this.mensajeError.set(err?.error?.mensaje || 'Error al obtener producto');
      },
    });
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.productoEnEdicion.set(null);
    this.form.reset();
  }

  guardarProducto(): void {
    if (this.form.invalid) return;

    this.guardando.set(true);
    this.mensajeError.set(null);

    const val = this.form.value;
    const edicion = this.productoEnEdicion();

    if (edicion) {
      const updateDto: ProductoActualizarRequest = {
        codigoBarras: val.codigoBarras!.trim().toUpperCase(),
        nombre: val.nombre!.trim(),
        descripcion: val.descripcion ? val.descripcion.trim() : null,
        categoriaId: Number(val.categoriaId),
        proveedorId: val.proveedorId ? Number(val.proveedorId) : null,
        costoCompra: Number(val.costoCompra),
        stockMinimo: Number(val.stockMinimo ?? 5),
        unidadMedida: val.unidadMedida!.trim(),
        porcentajeGananciaManual:
          val.porcentajeGananciaManual !== null && val.porcentajeGananciaManual !== undefined
            ? Number(val.porcentajeGananciaManual)
            : null,
      };

      this.productoService.actualizar(edicion.id, updateDto).subscribe({
        next: (res) => {
          this.guardando.set(false);
          this.cerrarModal();
          this.mensajeExito.set(`Producto "${res.nombre}" actualizado con precio de venta ${res.precioVenta}`);
          this.cargarProductos();
        },
        error: (err) => {
          this.guardando.set(false);
          this.mensajeError.set(err?.error?.mensaje || 'Error al actualizar producto');
        },
      });
    } else {
      const createDto: ProductoCrearRequest = {
        codigoBarras: val.codigoBarras!.trim().toUpperCase(),
        nombre: val.nombre!.trim(),
        descripcion: val.descripcion ? val.descripcion.trim() : null,
        categoriaId: Number(val.categoriaId),
        proveedorId: val.proveedorId ? Number(val.proveedorId) : null,
        costoCompra: Number(val.costoCompra),
        stockMinimo: Number(val.stockMinimo ?? 5),
        unidadMedida: val.unidadMedida!.trim(),
        porcentajeGananciaManual:
          val.porcentajeGananciaManual !== null && val.porcentajeGananciaManual !== undefined
            ? Number(val.porcentajeGananciaManual)
            : null,
        cantidadDesconocida: Boolean(val.cantidadDesconocida),
      };

      this.productoService.crear(createDto).subscribe({
        next: (res) => {
          this.guardando.set(false);
          this.cerrarModal();
          this.mensajeExito.set(`Producto "${res.nombre}" registrado con precio de venta ${res.precioVenta}`);
          this.cargarProductos();
        },
        error: (err) => {
          this.guardando.set(false);
          this.mensajeError.set(err?.error?.mensaje || 'Error al registrar producto');
        },
      });
    }
  }

  abrirModalEstado(prod: ProductoListado, reactivar: boolean): void {
    this.productoParaEstado.set(prod);
    this.accionEstado.set(reactivar);
    this.modalEstado.set(true);
  }

  ejecutarCambioEstado(): void {
    const prod = this.productoParaEstado();
    if (!prod) return;

    this.guardando.set(true);
    const reactivar = this.accionEstado();

    const request$ = reactivar
      ? this.productoService.reactivar(prod.id)
      : this.productoService.desactivar(prod.id);

    request$.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalEstado.set(false);
        this.productoParaEstado.set(null);
        this.mensajeExito.set(
          `Producto "${prod.nombre}" ${reactivar ? 'reactivado' : 'desactivado'} exitosamente.`
        );
        this.cargarProductos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(err?.error?.mensaje || 'Error al cambiar el estado del producto');
      },
    });
  }

  abrirModalFotos(prod: ProductoListado): void {
    this.productoParaFotos.set(prod);
    this.modalFotos.set(true);
  }

  cerrarModalFotos(): void {
    this.modalFotos.set(false);
    this.productoParaFotos.set(null);
  }

  onFotosActualizadas(): void {
    this.cargarProductos();
  }
}
