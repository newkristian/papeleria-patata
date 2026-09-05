import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InventarioService } from '../../../core/services/inventario.service';
import { ProductoService } from '../../../core/services/producto.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  FiltrosMovimientos,
  InventarioMovimiento,
  InventarioMovimientoRequest,
  AjusteInventarioRequest,
  TipoMovimiento,
} from '../../../core/models/inventario.model';
import { Pagina, ProductoListado } from '../../../core/models/producto.model';

@Component({
  selector: 'app-inventario-admin',
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="p-6 max-w-7xl mx-auto space-y-6">
      <!-- Encabezado Principal -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Control de Inventario</h1>
          <p class="text-sm text-gray-500 mt-1">
            Registro de movimientos físicos, auditoría de stock, ajustes y regularización de existencias iniciales.
          </p>
        </div>
        @if (authService.canManageProducts()) {
          <div class="flex flex-wrap items-center gap-2">
            <button
              type="button"
              (click)="abrirModalMovimiento('ENTRADA')"
              class="inline-flex items-center px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors">
              <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Registrar Entrada
            </button>
            <button
              type="button"
              (click)="abrirModalMovimiento('SALIDA')"
              class="inline-flex items-center px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors">
              <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
              </svg>
              Registrar Salida
            </button>
            <button
              type="button"
              (click)="abrirModalMovimiento('AJUSTE')"
              class="inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors">
              <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Ajuste de Stock
            </button>
          </div>
        }
      </div>

      <!-- Alertas de Éxito y Error -->
      @if (mensajeExito()) {
        <div class="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <svg class="w-5 h-5 text-emerald-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <span>{{ mensajeExito() }}</span>
          </div>
          <button type="button" (click)="mensajeExito.set(null)" class="text-emerald-600 hover:text-emerald-800 text-lg font-bold">&times;</button>
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
          <button type="button" (click)="mensajeError.set(null)" class="text-rose-600 hover:text-rose-800 text-lg font-bold">&times;</button>
        </div>
      }

      <!-- SECCIÓN DESTACADA: Productos con Cantidad Desconocida (Pendientes de conteo físico inicial) -->
      @if (productosPorContar().length > 0) {
        <div class="bg-amber-50/70 border-2 border-amber-300/80 rounded-2xl p-5 shadow-sm space-y-4">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-xl bg-amber-100 border border-amber-300 flex items-center justify-center shrink-0">
                <svg class="w-6 h-6 text-amber-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div>
                <h2 class="text-base font-bold text-amber-900">
                  Productos Pendientes de Conteo Físico Inicial ({{ productosPorContar().length }})
                </h2>
                <p class="text-xs text-amber-800">
                  Estos artículos se registraron con stock desconocido. Debes fijar su conteo real absoluto para habilitar salidas y regularizar el inventario.
                </p>
              </div>
            </div>
            <button
              type="button"
              (click)="cargarProductosPorContar()"
              class="text-xs font-semibold text-amber-800 hover:text-amber-900 bg-amber-100 hover:bg-amber-200 px-3 py-1.5 rounded-lg transition-colors inline-flex items-center self-start sm:self-auto">
              <svg class="w-3.5 h-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Actualizar
            </button>
          </div>

          <!-- Lista horizontal o grilla de productos por contar -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            @for (prod of productosPorContar(); track prod.id) {
              <div class="bg-white border border-amber-200 rounded-xl p-3.5 flex items-center justify-between shadow-xs">
                <div class="flex items-center space-x-3 overflow-hidden">
                  <div class="w-10 h-10 rounded-lg bg-gray-100 border border-gray-200 flex items-center justify-center shrink-0 overflow-hidden">
                    @if (prod.urlThumbnail) {
                      <img [src]="prod.urlThumbnail" [alt]="prod.nombre" class="w-full h-full object-cover" />
                    } @else {
                      <svg class="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    }
                  </div>
                  <div class="min-w-0">
                    <p class="text-sm font-semibold text-gray-900 truncate" [title]="prod.nombre">{{ prod.nombre }}</p>
                    <p class="text-xs font-mono text-gray-500">{{ prod.codigoBarras }}</p>
                  </div>
                </div>

                <button
                  type="button"
                  (click)="iniciarConteoInicial(prod)"
                  class="ml-3 shrink-0 px-3 py-1.5 bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors">
                  Conteo inicial
                </button>
              </div>
            }
          </div>
        </div>
      }

      <!-- Filtros de Historial de Movimientos -->
      <div class="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm space-y-4">
        <div class="flex items-center justify-between">
          <h2 class="text-base font-bold text-gray-900">Historial de Movimientos de Inventario</h2>
          <span class="text-xs text-gray-500 font-medium">
            Total: {{ totalElementos() }} registros
          </span>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Búsqueda rápida por producto -->
          <div>
            <label class="block text-xs font-bold text-gray-600 uppercase mb-1">Buscar Producto</label>
            <div class="relative">
              <input
                type="text"
                [value]="terminoBusquedaProducto()"
                (input)="onTerminoProductoInput($event)"
                placeholder="Nombre o código..."
                class="w-full px-3 py-2 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500" />
              @if (sugerenciasProductos().length > 0) {
                <div class="absolute left-0 right-0 top-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-30 max-h-48 overflow-y-auto divide-y divide-gray-100">
                  @for (p of sugerenciasProductos(); track p.id) {
                    <button
                      type="button"
                      (click)="seleccionarFiltroProducto(p)"
                      class="w-full text-left px-3 py-2 hover:bg-blue-50 text-xs flex items-center justify-between">
                      <span class="font-medium text-gray-800 truncate">{{ p.nombre }}</span>
                      <span class="font-mono text-gray-500 ml-2">{{ p.codigoBarras }}</span>
                    </button>
                  }
                </div>
              }
            </div>
            @if (filtroProductoSeleccionado()) {
              <div class="mt-1.5 flex items-center justify-between text-xs bg-blue-50 text-blue-700 px-2 py-1 rounded-md border border-blue-100">
                <span class="truncate font-medium">Filtrando: {{ filtroProductoSeleccionado()?.nombre }}</span>
                <button type="button" (click)="quitarFiltroProducto()" class="text-blue-800 font-bold ml-1">&times;</button>
              </div>
            }
          </div>

          <!-- Filtro por Tipo de Movimiento -->
          <div>
            <label class="block text-xs font-bold text-gray-600 uppercase mb-1">Tipo de Movimiento</label>
            <select
              [value]="filtroTipo()"
              (change)="onTipoChange($event)"
              class="w-full px-3 py-2 rounded-xl border border-gray-300 text-sm bg-white focus:ring-2 focus:ring-blue-500">
              <option value="">Todos los tipos</option>
              <option value="ENTRADA">Entrada (+)</option>
              <option value="SALIDA">Salida (-)</option>
              <option value="AJUSTE">Ajuste (Físico / Auditoría)</option>
            </select>
          </div>

          <!-- Fecha Desde -->
          <div>
            <label class="block text-xs font-bold text-gray-600 uppercase mb-1">Desde</label>
            <input
              type="date"
              [value]="filtroFechaInicio()"
              (change)="onFechaInicioChange($event)"
              class="w-full px-3 py-2 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-blue-500" />
          </div>

          <!-- Fecha Hasta -->
          <div>
            <label class="block text-xs font-bold text-gray-600 uppercase mb-1">Hasta</label>
            <input
              type="date"
              [value]="filtroFechaFin()"
              (change)="onFechaFinChange($event)"
              class="w-full px-3 py-2 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-blue-500" />
          </div>
        </div>

        <div class="flex items-center justify-between pt-2 border-t border-gray-100 text-sm">
          <button
            type="button"
            (click)="limpiarFiltros()"
            class="text-xs text-blue-600 hover:text-blue-800 font-medium">
            Limpiar filtros
          </button>
          <button
            type="button"
            (click)="cargarMovimientos()"
            class="px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-medium rounded-lg transition-colors flex items-center">
            <svg class="w-3.5 h-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refrescar tabla
          </button>
        </div>
      </div>

      <!-- Tabla de Historial de Movimientos -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        @if (cargando()) {
          <div class="p-12 text-center text-gray-500">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-2"></div>
            <p class="text-sm">Cargando movimientos de inventario...</p>
          </div>
        } @else if (movimientos().length === 0) {
          <div class="p-12 text-center text-gray-500">
            <svg class="w-12 h-12 mx-auto text-gray-300 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
            <p class="font-medium text-gray-700">No se encontraron movimientos de inventario</p>
            <p class="text-xs text-gray-400 mt-1">Registra una entrada, salida o ajuste para ver el historial.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="bg-gray-50/80 border-b border-gray-200 text-xs font-bold text-gray-500 uppercase tracking-wider">
                  <th class="py-3.5 px-6">Fecha / Hora</th>
                  <th class="py-3.5 px-6">Tipo</th>
                  <th class="py-3.5 px-6">Producto</th>
                  <th class="py-3.5 px-6 text-right">Cantidad</th>
                  @if (!authService.isVendedor()) {
                    <th class="py-3.5 px-6 text-right">Costo Unitario</th>
                  }
                  <th class="py-3.5 px-6">Motivo</th>
                  <th class="py-3.5 px-6">Usuario</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100 text-sm text-gray-700">
                @for (mov of movimientos(); track mov.id) {
                  <tr class="hover:bg-gray-50/50 transition-colors">
                    <!-- Fecha y Hora -->
                    <td class="py-3.5 px-6 text-xs text-gray-600 font-mono whitespace-nowrap">
                      {{ mov.fechaMovimiento | date:'dd/MM/yyyy HH:mm' }}
                    </td>

                    <!-- Tipo con Badge -->
                    <td class="py-3.5 px-6 whitespace-nowrap">
                      @if (mov.tipo === 'ENTRADA') {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
                          <svg class="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4" />
                          </svg>
                          Entrada
                        </span>
                      } @else if (mov.tipo === 'SALIDA') {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-100 text-rose-800 border border-rose-200">
                          <svg class="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M20 12H4" />
                          </svg>
                          Salida
                        </span>
                      } @else {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-200">
                          <svg class="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                          </svg>
                          Ajuste
                        </span>
                      }
                    </td>

                    <!-- Producto -->
                    <td class="py-3.5 px-6">
                      <div class="font-medium text-gray-900 leading-snug">{{ mov.productoNombre }}</div>
                      <div class="text-xs font-mono text-gray-500">{{ mov.productoCodigoBarras }}</div>
                    </td>

                    <!-- Cantidad -->
                    <td class="py-3.5 px-6 text-right font-bold text-sm" [class.text-emerald-700]="mov.tipo === 'ENTRADA'" [class.text-rose-700]="mov.tipo === 'SALIDA'">
                      @if (mov.tipo === 'ENTRADA') { +{{ mov.cantidad }} }
                      @else if (mov.tipo === 'SALIDA') { -{{ mov.cantidad }} }
                      @else { {{ mov.cantidad }} }
                    </td>

                    <!-- Costo Unitario (Oculto si VENDEDOR) -->
                    @if (!authService.isVendedor()) {
                      <td class="py-3.5 px-6 text-right font-mono text-xs text-gray-700">
                        @if (mov.costoUnitario !== null && mov.costoUnitario !== undefined) {
                          {{ mov.costoUnitario | currency:'USD':'symbol':'1.2-2' }}
                        } @else {
                          <span class="text-gray-400 italic">—</span>
                        }
                      </td>
                    }

                    <!-- Motivo -->
                    <td class="py-3.5 px-6 text-xs text-gray-600 max-w-xs truncate" [title]="mov.motivo">
                      {{ mov.motivo }}
                    </td>

                    <!-- Usuario -->
                    <td class="py-3.5 px-6 text-xs text-gray-600 whitespace-nowrap">
                      {{ mov.usuarioNombre }}
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
                Página <span class="font-bold">{{ paginaActual() + 1 }}</span> de <span class="font-bold">{{ totalPaginas() }}</span>
                ({{ totalElementos() }} movimientos)
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

      <!-- MODAL: REGISTRAR ENTRADA / SALIDA -->
      @if (modalMovimientoAbierto()) {
        <div class="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-lg p-6 space-y-5 animate-in fade-in zoom-in-95">
            <!-- Header Modal -->
            <div class="flex items-center justify-between border-b border-gray-100 pb-3">
              <div class="flex items-center space-x-2">
                @if (tipoMovimientoModal() === 'ENTRADA') {
                  <span class="w-8 h-8 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-lg">+</span>
                  <h3 class="text-lg font-bold text-gray-900">Registrar Entrada de Inventario</h3>
                } @else if (tipoMovimientoModal() === 'SALIDA') {
                  <span class="w-8 h-8 rounded-full bg-rose-100 text-rose-700 flex items-center justify-center font-bold text-lg">-</span>
                  <h3 class="text-lg font-bold text-gray-900">Registrar Salida de Inventario</h3>
                } @else {
                  <span class="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm">±</span>
                  <h3 class="text-lg font-bold text-gray-900">Ajuste de Existencias</h3>
                }
              </div>
              <button type="button" (click)="cerrarModal()" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
            </div>

            <!-- Formulario si es ENTRADA o SALIDA -->
            @if (tipoMovimientoModal() === 'ENTRADA' || tipoMovimientoModal() === 'SALIDA') {
              <form [formGroup]="formMovimiento" (ngSubmit)="guardarMovimiento()" class="space-y-4">
                <!-- Buscador / Selección de Producto -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Producto *</label>
                  @if (!productoSeleccionado()) {
                    <div class="relative">
                      <input
                        type="text"
                        [value]="terminoModalProducto()"
                        (input)="onTerminoModalInput($event)"
                        placeholder="Escribe código de barras o nombre..."
                        class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
                      @if (sugerenciasModal().length > 0) {
                        <div class="absolute left-0 right-0 top-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-30 max-h-48 overflow-y-auto divide-y divide-gray-100">
                          @for (p of sugerenciasModal(); track p.id) {
                            <button
                              type="button"
                              (click)="seleccionarProductoModal(p)"
                              class="w-full text-left px-3 py-2 hover:bg-blue-50 text-xs flex items-center justify-between">
                              <div>
                                <span class="font-medium text-gray-800">{{ p.nombre }}</span>
                                <span class="font-mono text-gray-500 ml-2">({{ p.codigoBarras }})</span>
                              </div>
                              <span class="text-gray-500 text-[11px]">Stock: {{ p.cantidadDesconocida ? 'Por contar' : p.stockActual }}</span>
                            </button>
                          }
                        </div>
                      }
                    </div>
                  } @else {
                    <div class="p-3 bg-gray-50 border border-gray-200 rounded-xl flex items-center justify-between">
                      <div>
                        <p class="text-sm font-semibold text-gray-900">{{ productoSeleccionado()?.nombre }}</p>
                        <p class="text-xs font-mono text-gray-500">
                          Código: {{ productoSeleccionado()?.codigoBarras }} |
                          Stock actual:
                          @if (productoSeleccionado()?.cantidadDesconocida) {
                            <span class="font-bold text-amber-600">Por contar</span>
                          } @else {
                            <span class="font-bold text-gray-800">{{ productoSeleccionado()?.stockActual }}</span>
                          }
                        </p>
                      </div>
                      <button
                        type="button"
                        (click)="deseleccionarProductoModal()"
                        class="text-xs text-blue-600 hover:text-blue-800 font-medium">
                        Cambiar
                      </button>
                    </div>
                  }
                  @if (tipoMovimientoModal() === 'SALIDA' && productoSeleccionado()?.cantidadDesconocida) {
                    <p class="mt-1 text-xs text-rose-600 font-medium">
                      Atención: No puedes registrar salidas de un producto con cantidad desconocida. Realiza primero un ajuste o conteo inicial.
                    </p>
                  }
                </div>

                <!-- Cantidad -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Cantidad *</label>
                  <input
                    type="number"
                    min="1"
                    step="1"
                    formControlName="cantidad"
                    placeholder="Ej. 10"
                    class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
                  @if (tipoMovimientoModal() === 'SALIDA' && esSuperaStock()) {
                    <p class="mt-1 text-xs text-rose-600 font-medium">
                      La cantidad a retirar supera la existencia actual ({{ productoSeleccionado()?.stockActual }} disponibles).
                    </p>
                  }
                </div>

                <!-- Costo Unitario -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Costo Unitario ($) *</label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    formControlName="costoUnitario"
                    placeholder="Ej. 15.50"
                    class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
                  <p class="mt-0.5 text-[11px] text-gray-500">
                    Costo de adquisición de la mercancía para efectos de costeo y valuación.
                  </p>
                </div>

                <!-- Motivo -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Motivo / Justificación *</label>
                  <textarea
                    rows="2"
                    formControlName="motivo"
                    placeholder="Ej. Recepción de factura F-102, reposición de mercancía dañada, merma..."
                    class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500"></textarea>
                </div>

                <!-- Botones Acción -->
                <div class="flex items-center justify-end space-x-3 pt-3 border-t border-gray-100">
                  <button
                    type="button"
                    (click)="cerrarModal()"
                    class="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-xl transition-colors">
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    [disabled]="formMovimiento.invalid || guardando() || (tipoMovimientoModal() === 'SALIDA' && esSalidaInvalida())"
                    [class.bg-emerald-600]="tipoMovimientoModal() === 'ENTRADA'"
                    [class.hover:bg-emerald-700]="tipoMovimientoModal() === 'ENTRADA'"
                    [class.bg-rose-600]="tipoMovimientoModal() === 'SALIDA'"
                    [class.hover:bg-rose-700]="tipoMovimientoModal() === 'SALIDA'"
                    class="px-5 py-2.5 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors disabled:opacity-50 flex items-center">
                    @if (guardando()) {
                      <div class="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    }
                    {{ tipoMovimientoModal() === 'ENTRADA' ? 'Confirmar Entrada' : 'Confirmar Salida' }}
                  </button>
                </div>
              </form>
            }

            <!-- Formulario si es AJUSTE DE STOCK -->
            @if (tipoMovimientoModal() === 'AJUSTE') {
              <form [formGroup]="formAjuste" (ngSubmit)="guardarAjuste()" class="space-y-4">
                <!-- Buscador / Selección de Producto -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Producto a Ajustar *</label>
                  @if (!productoSeleccionado()) {
                    <div class="relative">
                      <input
                        type="text"
                        [value]="terminoModalProducto()"
                        (input)="onTerminoModalInput($event)"
                        placeholder="Escribe código de barras o nombre..."
                        class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
                      @if (sugerenciasModal().length > 0) {
                        <div class="absolute left-0 right-0 top-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-30 max-h-48 overflow-y-auto divide-y divide-gray-100">
                          @for (p of sugerenciasModal(); track p.id) {
                            <button
                              type="button"
                              (click)="seleccionarProductoModal(p)"
                              class="w-full text-left px-3 py-2 hover:bg-blue-50 text-xs flex items-center justify-between">
                              <div>
                                <span class="font-medium text-gray-800">{{ p.nombre }}</span>
                                <span class="font-mono text-gray-500 ml-2">({{ p.codigoBarras }})</span>
                              </div>
                              <span class="text-gray-500 text-[11px]">Stock: {{ p.cantidadDesconocida ? 'Por contar' : p.stockActual }}</span>
                            </button>
                          }
                        </div>
                      }
                    </div>
                  } @else {
                    <div class="p-3 bg-gray-50 border border-gray-200 rounded-xl flex items-center justify-between">
                      <div>
                        <p class="text-sm font-semibold text-gray-900">{{ productoSeleccionado()?.nombre }}</p>
                        <p class="text-xs font-mono text-gray-500">
                          Código: {{ productoSeleccionado()?.codigoBarras }} |
                          Stock actual:
                          @if (productoSeleccionado()?.cantidadDesconocida) {
                            <span class="font-bold text-amber-600">Por contar (desconocido)</span>
                          } @else {
                            <span class="font-bold text-gray-800">{{ productoSeleccionado()?.stockActual }}</span>
                          }
                        </p>
                      </div>
                      <button
                        type="button"
                        (click)="deseleccionarProductoModal()"
                        class="text-xs text-blue-600 hover:text-blue-800 font-medium">
                        Cambiar
                      </button>
                    </div>
                  }
                </div>

                <!-- Tipo de Ajuste: Absoluto vs Relativo -->
                <div class="space-y-2">
                  <span class="block text-xs font-bold text-gray-700 uppercase">Modo de Ajuste *</span>
                  <div class="grid grid-cols-2 gap-2">
                    <label
                      class="flex items-center space-x-2 p-2.5 rounded-xl border cursor-pointer text-xs transition-colors"
                      [class.bg-blue-50]="formAjuste.get('esFijarStockAbsoluto')?.value"
                      [class.border-blue-300]="formAjuste.get('esFijarStockAbsoluto')?.value"
                      [class.border-gray-200]="!formAjuste.get('esFijarStockAbsoluto')?.value">
                      <input
                        type="radio"
                        [value]="true"
                        formControlName="esFijarStockAbsoluto"
                        class="text-blue-600 focus:ring-blue-500" />
                      <div>
                        <span class="font-bold text-gray-900 block">Fijar Stock Absoluto</span>
                        <span class="text-gray-500 text-[11px]">Conteo físico real</span>
                      </div>
                    </label>

                    <label
                      class="flex items-center space-x-2 p-2.5 rounded-xl border cursor-pointer text-xs transition-colors"
                      [class.opacity-50]="productoSeleccionado()?.cantidadDesconocida"
                      [class.bg-blue-50]="!formAjuste.get('esFijarStockAbsoluto')?.value"
                      [class.border-blue-300]="!formAjuste.get('esFijarStockAbsoluto')?.value"
                      [class.border-gray-200]="formAjuste.get('esFijarStockAbsoluto')?.value">
                      <input
                        type="radio"
                        [value]="false"
                        [disabled]="productoSeleccionado()?.cantidadDesconocida ?? false"
                        formControlName="esFijarStockAbsoluto"
                        class="text-blue-600 focus:ring-blue-500" />
                      <div>
                        <span class="font-bold text-gray-900 block">Ajuste Relativo (+/-)</span>
                        <span class="text-gray-500 text-[11px]">Sumar o restar unidades</span>
                      </div>
                    </label>
                  </div>
                  @if (productoSeleccionado()?.cantidadDesconocida) {
                    <p class="text-[11px] text-amber-700">
                      * Este producto se encuentra "Por contar". Se requiere fijar el stock absoluto para regularizarlo.
                    </p>
                  }
                </div>

                <!-- Cantidad -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">
                    {{ formAjuste.get('esFijarStockAbsoluto')?.value ? 'Nueva Existencia Total *' : 'Cantidad a Ajustar (+ o -) *' }}
                  </label>
                  <input
                    type="number"
                    step="1"
                    [min]="formAjuste.get('esFijarStockAbsoluto')?.value ? 0 : null"
                    formControlName="cantidad"
                    [placeholder]="formAjuste.get('esFijarStockAbsoluto')?.value ? 'Ej. 25' : 'Ej. -3 o 5'"
                    class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
                </div>

                <!-- Nuevo Costo de Compra (Opcional) -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">
                    Nuevo Costo de Compra ($) (Opcional)
                  </label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    formControlName="nuevoCostoCompra"
                    placeholder="Dejar vacío para conservar el costo actual"
                    class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
                </div>

                <!-- Motivo -->
                <div>
                  <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Motivo del Ajuste *</label>
                  <textarea
                    rows="2"
                    formControlName="motivo"
                    placeholder="Ej. Conteo físico trimestral, corrección de inventario, merma..."
                    class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500"></textarea>
                </div>

                <!-- Botones Acción -->
                <div class="flex items-center justify-end space-x-3 pt-3 border-t border-gray-100">
                  <button
                    type="button"
                    (click)="cerrarModal()"
                    class="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-xl transition-colors">
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    [disabled]="formAjuste.invalid || guardando()"
                    class="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors disabled:opacity-50 flex items-center">
                    @if (guardando()) {
                      <div class="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    }
                    Aplicar Ajuste
                  </button>
                </div>
              </form>
            }
          </div>
        </div>
      }
    </div>
  `,
})
export class InventarioAdminComponent implements OnInit {
  readonly inventarioService = inject(InventarioService);
  readonly productoService = inject(ProductoService);
  readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  // Estados de datos
  readonly movimientos = signal<InventarioMovimiento[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);

  // Sección de productos pendientes de conteo físico inicial
  readonly productosPorContar = signal<ProductoListado[]>([]);
  readonly cargandoPorContar = signal(false);

  // Filtros de historial
  readonly filtroTipo = signal<TipoMovimiento | ''>('');
  readonly filtroProductoId = signal<number | null>(null);
  readonly filtroProductoSeleccionado = signal<ProductoListado | null>(null);
  readonly terminoBusquedaProducto = signal('');
  readonly sugerenciasProductos = signal<ProductoListado[]>([]);
  readonly filtroFechaInicio = signal('');
  readonly filtroFechaFin = signal('');

  // Paginación
  readonly paginaActual = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalElementos = signal(0);

  // Modal de movimientos y ajustes
  readonly modalMovimientoAbierto = signal(false);
  readonly tipoMovimientoModal = signal<'ENTRADA' | 'SALIDA' | 'AJUSTE'>('ENTRADA');
  readonly productoSeleccionado = signal<ProductoListado | null>(null);
  readonly terminoModalProducto = signal('');
  readonly sugerenciasModal = signal<ProductoListado[]>([]);

  // Notificaciones
  readonly mensajeExito = signal<string | null>(null);
  readonly mensajeError = signal<string | null>(null);

  // Formularios
  readonly formMovimiento = this.fb.group({
    productoId: [null as number | null, [Validators.required]],
    cantidad: [null as number | null, [Validators.required, Validators.min(1)]],
    motivo: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
    costoUnitario: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  readonly formAjuste = this.fb.group({
    productoId: [null as number | null, [Validators.required]],
    cantidad: [null as number | null, [Validators.required]],
    motivo: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
    nuevoCostoCompra: [null as number | null, [Validators.min(0.01)]],
    esFijarStockAbsoluto: [true],
  });

  ngOnInit(): void {
    this.cargarProductosPorContar();
    this.cargarMovimientos();
  }

  cargarProductosPorContar(): void {
    this.cargandoPorContar.set(true);
    this.productoService.buscarAvanzado({ activo: true }, 0, 50).subscribe({
      next: (pagina) => {
        const sinContar = pagina.content.filter((p) => p.cantidadDesconocida);
        this.productosPorContar.set(sinContar);
        this.cargandoPorContar.set(false);
      },
      error: () => this.cargandoPorContar.set(false),
    });
  }

  cargarMovimientos(): void {
    this.cargando.set(true);

    const filtros: FiltrosMovimientos = {
      tipo: this.filtroTipo() ? (this.filtroTipo() as TipoMovimiento) : null,
      productoId: this.filtroProductoId(),
      fechaInicio: this.filtroFechaInicio() ? `${this.filtroFechaInicio()}T00:00:00` : null,
      fechaFin: this.filtroFechaFin() ? `${this.filtroFechaFin()}T23:59:59` : null,
    };

    this.inventarioService.listarMovimientos(filtros, this.paginaActual(), 20).subscribe({
      next: (pagina: Pagina<InventarioMovimiento>) => {
        this.movimientos.set(pagina.content);
        this.totalPaginas.set(pagina.page.totalPages);
        this.totalElementos.set(pagina.page.totalElements);
        this.cargando.set(false);
      },
      error: (err: any) => {
        this.mensajeError.set(err?.error?.mensaje || 'Error al consultar historial de movimientos');
        this.cargando.set(false);
      },
    });
  }

  onTerminoProductoInput(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.terminoBusquedaProducto.set(term);

    if (term.trim().length >= 2) {
      this.productoService.buscarAvanzado({ termino: term.trim() }, 0, 5).subscribe({
        next: (pagina) => this.sugerenciasProductos.set(pagina.content),
        error: () => this.sugerenciasProductos.set([]),
      });
    } else {
      this.sugerenciasProductos.set([]);
    }
  }

  seleccionarFiltroProducto(p: ProductoListado): void {
    this.filtroProductoId.set(p.id);
    this.filtroProductoSeleccionado.set(p);
    this.terminoBusquedaProducto.set('');
    this.sugerenciasProductos.set([]);
    this.paginaActual.set(0);
    this.cargarMovimientos();
  }

  quitarFiltroProducto(): void {
    this.filtroProductoId.set(null);
    this.filtroProductoSeleccionado.set(null);
    this.paginaActual.set(0);
    this.cargarMovimientos();
  }

  onTipoChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value as TipoMovimiento | '';
    this.filtroTipo.set(val);
    this.paginaActual.set(0);
    this.cargarMovimientos();
  }

  onFechaInicioChange(event: Event): void {
    this.filtroFechaInicio.set((event.target as HTMLInputElement).value);
    this.paginaActual.set(0);
    this.cargarMovimientos();
  }

  onFechaFinChange(event: Event): void {
    this.filtroFechaFin.set((event.target as HTMLInputElement).value);
    this.paginaActual.set(0);
    this.cargarMovimientos();
  }

  limpiarFiltros(): void {
    this.filtroTipo.set('');
    this.filtroProductoId.set(null);
    this.filtroProductoSeleccionado.set(null);
    this.terminoBusquedaProducto.set('');
    this.sugerenciasProductos.set([]);
    this.filtroFechaInicio.set('');
    this.filtroFechaFin.set('');
    this.paginaActual.set(0);
    this.cargarMovimientos();
  }

  cambiarPagina(nuevaPagina: number): void {
    this.paginaActual.set(nuevaPagina);
    this.cargarMovimientos();
  }

  // MODALES Y ACCIONES
  abrirModalMovimiento(tipo: 'ENTRADA' | 'SALIDA' | 'AJUSTE', prod?: ProductoListado): void {
    this.tipoMovimientoModal.set(tipo);
    this.productoSeleccionado.set(prod || null);
    this.terminoModalProducto.set('');
    this.sugerenciasModal.set([]);
    this.mensajeError.set(null);

    if (tipo === 'ENTRADA' || tipo === 'SALIDA') {
      this.formMovimiento.reset({
        productoId: prod?.id ?? null,
        cantidad: null,
        motivo: '',
        costoUnitario: null,
      });
      if (prod) {
        // Consultar costo para prellenar
        this.productoService.obtenerPorId(prod.id).subscribe({
          next: (detalle) => this.formMovimiento.patchValue({ costoUnitario: detalle.costoCompra }),
        });
      }
    } else {
      this.formAjuste.reset({
        productoId: prod?.id ?? null,
        cantidad: null,
        motivo: prod?.cantidadDesconocida ? 'Conteo físico inicial de inventario' : '',
        nuevoCostoCompra: null,
        esFijarStockAbsoluto: true,
      });
    }

    this.modalMovimientoAbierto.set(true);
  }

  iniciarConteoInicial(prod: ProductoListado): void {
    this.abrirModalMovimiento('AJUSTE', prod);
  }

  onTerminoModalInput(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.terminoModalProducto.set(term);

    if (term.trim().length >= 2) {
      this.productoService.buscarAvanzado({ termino: term.trim() }, 0, 5).subscribe({
        next: (pagina) => this.sugerenciasModal.set(pagina.content),
        error: () => this.sugerenciasModal.set([]),
      });
    } else {
      this.sugerenciasModal.set([]);
    }
  }

  seleccionarProductoModal(p: ProductoListado): void {
    this.productoSeleccionado.set(p);
    this.terminoModalProducto.set('');
    this.sugerenciasModal.set([]);

    if (this.tipoMovimientoModal() === 'ENTRADA' || this.tipoMovimientoModal() === 'SALIDA') {
      this.formMovimiento.patchValue({ productoId: p.id });
      this.productoService.obtenerPorId(p.id).subscribe({
        next: (detalle) => this.formMovimiento.patchValue({ costoUnitario: detalle.costoCompra }),
      });
    } else {
      this.formAjuste.patchValue({
        productoId: p.id,
        esFijarStockAbsoluto: p.cantidadDesconocida ? true : this.formAjuste.get('esFijarStockAbsoluto')?.value,
        motivo: p.cantidadDesconocida ? 'Conteo físico inicial de inventario' : this.formAjuste.get('motivo')?.value,
      });
    }
  }

  deseleccionarProductoModal(): void {
    this.productoSeleccionado.set(null);
    this.formMovimiento.patchValue({ productoId: null, costoUnitario: null });
    this.formAjuste.patchValue({ productoId: null });
  }

  esSalidaInvalida(): boolean {
    const prod = this.productoSeleccionado();
    if (!prod) return true;
    if (prod.cantidadDesconocida) return true;
    const cant = this.formMovimiento.get('cantidad')?.value;
    if (!cant || cant <= 0) return true;
    return cant > prod.stockActual;
  }

  esSuperaStock(): boolean {
    const prod = this.productoSeleccionado();
    if (!prod || prod.cantidadDesconocida) return false;
    const cant = this.formMovimiento.get('cantidad')?.value;
    return typeof cant === 'number' && cant > prod.stockActual;
  }

  cerrarModal(): void {
    this.modalMovimientoAbierto.set(false);
    this.productoSeleccionado.set(null);
    this.formMovimiento.reset();
    this.formAjuste.reset();
  }

  guardarMovimiento(): void {
    if (this.formMovimiento.invalid) return;
    if (this.tipoMovimientoModal() === 'SALIDA' && this.esSalidaInvalida()) return;

    this.guardando.set(true);
    this.mensajeError.set(null);

    const val = this.formMovimiento.value;
    const request: InventarioMovimientoRequest = {
      productoId: Number(val.productoId),
      cantidad: Number(val.cantidad),
      motivo: val.motivo!.trim(),
      costoUnitario: Number(val.costoUnitario),
    };

    const peticion$ =
      this.tipoMovimientoModal() === 'ENTRADA'
        ? this.inventarioService.registrarEntrada(request)
        : this.inventarioService.registrarSalida(request);

    peticion$.subscribe({
      next: (mov) => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mensajeExito.set(
          `Movimiento de ${mov.tipo} registrado exitosamente (${mov.cantidad} unidades para "${mov.productoNombre}").`
        );
        this.cargarMovimientos();
        this.cargarProductosPorContar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(err?.error?.mensaje || 'Error al registrar movimiento');
      },
    });
  }

  guardarAjuste(): void {
    if (this.formAjuste.invalid) return;

    this.guardando.set(true);
    this.mensajeError.set(null);

    const val = this.formAjuste.value;
    const request: AjusteInventarioRequest = {
      productoId: Number(val.productoId),
      cantidad: Number(val.cantidad),
      motivo: val.motivo!.trim(),
      nuevoCostoCompra: val.nuevoCostoCompra ? Number(val.nuevoCostoCompra) : null,
      esFijarStockAbsoluto: Boolean(val.esFijarStockAbsoluto),
    };

    this.inventarioService.ajustarInventario(request).subscribe({
      next: (prodActualizado) => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mensajeExito.set(
          `Ajuste de inventario aplicado a "${prodActualizado.nombre}". Nuevo stock: ${prodActualizado.stockActual}`
        );
        this.cargarMovimientos();
        this.cargarProductosPorContar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(err?.error?.mensaje || 'Error al aplicar ajuste de inventario');
      },
    });
  }
}
