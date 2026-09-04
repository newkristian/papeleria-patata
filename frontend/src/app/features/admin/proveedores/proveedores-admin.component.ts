import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { AuthService } from '../../../core/services/auth.service';
import { Proveedor } from '../../../core/models/proveedor.model';
import { Pagina } from '../../../core/models/producto.model';

@Component({
  selector: 'app-proveedores-admin',
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="p-6 max-w-7xl mx-auto space-y-6">
      <!-- Encabezado de la Sección -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Gestión de Proveedores</h1>
          <p class="text-sm text-gray-500 mt-1">
            Administra los proveedores de la papelería, porcentajes de comisión y estado de proveedor pendiente.
          </p>
        </div>
        @if (authService.canManageProviders()) {
          <button
            type="button"
            (click)="abrirModalCrear()"
            class="inline-flex items-center justify-center px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors">
            <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nuevo Proveedor
          </button>
        }
      </div>

      <!-- Barra de Filtros y Búsqueda -->
      <div class="bg-white p-4 rounded-2xl border border-gray-200 shadow-sm flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div class="relative flex-1 w-full">
          <svg class="w-5 h-5 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            [value]="terminoBusqueda()"
            (input)="onTerminoChange($event)"
            placeholder="Buscar por nombre, RFC o contacto..."
            class="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" />
        </div>

        <div class="flex items-center space-x-3 w-full sm:w-auto">
          <label class="text-xs font-bold uppercase tracking-wider text-gray-500 shrink-0">Estado:</label>
          <select
            [value]="filtroActivo() === null ? 'todos' : (filtroActivo() ? 'activos' : 'inactivos')"
            (change)="onFiltroActivoChange($event)"
            class="px-3 py-2 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="todos">Todos los estados</option>
            <option value="activos">Solo activos</option>
            <option value="inactivos">Solo inactivos</option>
          </select>
        </div>
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

      <!-- Tabla de Proveedores -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        @if (cargando()) {
          <div class="p-12 text-center text-gray-500">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-3"></div>
            <p class="text-sm">Cargando proveedores...</p>
          </div>
        } @else if (proveedores().length === 0) {
          <div class="p-12 text-center">
            <div class="w-12 h-12 rounded-full bg-gray-100 text-gray-400 mx-auto flex items-center justify-center mb-3">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <h3 class="text-base font-bold text-gray-900">No se encontraron proveedores</h3>
            <p class="text-sm text-gray-500 mt-1">Prueba ajustando los términos de búsqueda o filtros.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="border-b border-gray-200 bg-gray-50/75 text-xs font-bold uppercase tracking-wider text-gray-500">
                  <th class="py-3.5 px-6">Proveedor</th>
                  <th class="py-3.5 px-6">RFC / Contacto</th>
                  <th class="py-3.5 px-6">Comisión</th>
                  <th class="py-3.5 px-6">Estado</th>
                  <th class="py-3.5 px-6 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100 text-sm text-gray-700">
                @for (prov of proveedores(); track prov.id) {
                  <tr class="hover:bg-gray-50/50 transition-colors" [class.bg-gray-50]="!prov.activo">
                    <td class="py-3.5 px-6">
                      <div class="font-semibold text-gray-900 flex items-center space-x-2">
                        <span>{{ prov.nombre }}</span>
                        @if (prov.sistema) {
                          <span class="inline-flex items-center px-2 py-0.5 rounded text-2xs font-bold bg-purple-100 text-purple-800">
                            SISTEMA
                          </span>
                        }
                      </div>
                      <div class="text-xs text-gray-500 mt-0.5">
                        {{ prov.email || 'Sin correo' }} · {{ prov.telefono || 'Sin teléfono' }}
                      </div>
                    </td>
                    <td class="py-3.5 px-6">
                      <div class="font-mono text-xs text-gray-700 font-medium">{{ prov.rfc || 'Sin RFC' }}</div>
                      <div class="text-xs text-gray-500">{{ prov.contacto || 'Sin contacto' }}</div>
                    </td>
                    <td class="py-3.5 px-6 font-semibold text-gray-900">
                      {{ prov.porcentajeComision }}%
                    </td>
                    <td class="py-3.5 px-6">
                      @if (prov.activo) {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
                          Activo
                        </span>
                      } @else {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-200 text-gray-700">
                          Inactivo
                        </span>
                      }
                    </td>
                    <td class="py-3.5 px-6 text-right space-x-2">
                      @if (prov.sistema) {
                        <span class="text-xs text-gray-400 italic">Configuración protegida</span>
                      } @else {
                        @if (authService.canManageProviders()) {
                          <button
                            type="button"
                            (click)="abrirModalEditar(prov)"
                            class="inline-flex items-center px-2.5 py-1.5 text-xs font-medium rounded-lg text-blue-700 bg-blue-50 hover:bg-blue-100 transition-colors">
                            Editar
                          </button>
                        }

                        @if (authService.canDeactivateProviders() && prov.activo) {
                          <button
                            type="button"
                            (click)="iniciarDesactivacion(prov)"
                            class="inline-flex items-center px-2.5 py-1.5 text-xs font-medium rounded-lg text-rose-700 bg-rose-50 hover:bg-rose-100 transition-colors">
                            Desactivar
                          </button>
                        }
                      }
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
                ({{ totalElementos() }} proveedores)
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

      <!-- Modal de Creación / Edición -->
      @if (modalAbierto()) {
        <div class="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-lg p-6 space-y-5 animate-in fade-in zoom-in-95">
            <div class="flex items-center justify-between border-b border-gray-100 pb-3">
              <h3 class="text-lg font-bold text-gray-900">
                {{ proveedorEnEdicion() ? 'Editar Proveedor' : 'Nuevo Proveedor' }}
              </h3>
              <button
                type="button"
                (click)="cerrarModal()"
                class="text-gray-400 hover:text-gray-600 text-xl font-bold leading-none">
                &times;
              </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardarProveedor()" class="space-y-4">
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                  Nombre de la empresa o proveedor *
                </label>
                <input
                  type="text"
                  formControlName="nombre"
                  placeholder="Ej. Papelera Nacional S.A."
                  class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                @if (form.get('nombre')?.touched && form.get('nombre')?.invalid) {
                  <p class="text-xs text-rose-600 mt-1">El nombre es obligatorio (máximo 255 caracteres).</p>
                }
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">RFC</label>
                  <input
                    type="text"
                    formControlName="rfc"
                    placeholder="ABCD123456XYZ"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm uppercase focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  @if (form.get('rfc')?.touched && form.get('rfc')?.invalid) {
                    <p class="text-xs text-rose-600 mt-1">Formato de RFC no válido.</p>
                  }
                </div>

                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Comisión (%) *</label>
                  <input
                    type="number"
                    step="0.01"
                    formControlName="porcentajeComision"
                    placeholder="0.00"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  @if (form.get('porcentajeComision')?.touched && form.get('porcentajeComision')?.invalid) {
                    <p class="text-xs text-rose-600 mt-1">Debe estar entre 0.00 y 100.00%.</p>
                  }
                </div>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Teléfono</label>
                  <input
                    type="text"
                    formControlName="telefono"
                    placeholder="5551234567"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Contacto</label>
                  <input
                    type="text"
                    formControlName="contacto"
                    placeholder="Nombre del ejecutivo"
                    class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">Correo Electrónico</label>
                <input
                  type="email"
                  formControlName="email"
                  placeholder="ventas@proveedor.com"
                  class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                @if (form.get('email')?.touched && form.get('email')?.invalid) {
                  <p class="text-xs text-rose-600 mt-1">Ingrese un formato de correo electrónico válido.</p>
                }
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
                  {{ guardando() ? 'Guardando...' : (proveedorEnEdicion() ? 'Actualizar' : 'Crear') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }

      <!-- Modal de Confirmación de Desactivación con Conteo de Productos -->
      @if (modalDesactivacion()) {
        <div class="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-md p-6 space-y-4 animate-in fade-in zoom-in-95">
            <div class="w-12 h-12 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center mx-auto mb-2">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>

            <h3 class="text-lg font-bold text-gray-900 text-center">
              ¿Desactivar a "{{ proveedorParaDesactivar()?.nombre }}"?
            </h3>

            <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 text-amber-900 text-xs sm:text-sm leading-relaxed">
              <p class="font-semibold mb-1">Impacto en el catálogo:</p>
              <p>
                Este proveedor tiene actualmente <strong class="text-amber-950 font-bold">{{ conteoProductosAfectados() }} productos</strong> asociados.
              </p>
              <p class="mt-2">
                Al desactivarlo, todos sus productos serán reasignados automáticamente al proveedor <strong class="text-purple-700 font-bold">PENDIENTE</strong> del sistema para que puedan continuar comercializándose o sean auditados.
              </p>
            </div>

            <div class="flex items-center justify-end space-x-3 pt-2">
              <button
                type="button"
                (click)="cancelarDesactivacion()"
                class="px-4 py-2 border border-gray-300 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">
                Cancelar
              </button>
              <button
                type="button"
                [disabled]="guardando()"
                (click)="ejecutarDesactivacion()"
                class="px-4 py-2 bg-rose-600 hover:bg-rose-700 disabled:opacity-50 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors">
                {{ guardando() ? 'Desactivando...' : 'Confirmar Desactivación' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class ProveedoresAdminComponent implements OnInit {
  readonly proveedorService = inject(ProveedorService);
  readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly proveedores = signal<Proveedor[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);

  readonly terminoBusqueda = signal('');
  readonly filtroActivo = signal<boolean | null>(null);
  readonly paginaActual = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalElementos = signal(0);

  readonly modalAbierto = signal(false);
  readonly proveedorEnEdicion = signal<Proveedor | null>(null);

  readonly modalDesactivacion = signal(false);
  readonly proveedorParaDesactivar = signal<Proveedor | null>(null);
  readonly conteoProductosAfectados = signal<number>(0);

  readonly mensajeExito = signal<string | null>(null);
  readonly mensajeError = signal<string | null>(null);

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(255)]],
    rfc: ['', [Validators.pattern('^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$')]],
    porcentajeComision: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    telefono: ['', [Validators.pattern('^[0-9+(). -]{7,50}$')]],
    contacto: ['', [Validators.maxLength(255)]],
    email: ['', [Validators.email, Validators.maxLength(255)]],
  });

  ngOnInit(): void {
    this.cargarProveedores();
  }

  cargarProveedores(): void {
    const termino = this.terminoBusqueda().trim();
    this.cargando.set(true);

    if (termino !== '') {
      // Invocamos el endpoint de búsqueda únicamente si el campo de búsqueda no está vacío
      this.proveedorService.buscar(
        termino,
        this.filtroActivo(),
        this.paginaActual(),
        15
      ).subscribe({
        next: (pagina: Pagina<Proveedor>) => {
          this.proveedores.set(pagina.content);
          this.totalPaginas.set(pagina.page.totalPages);
          this.totalElementos.set(pagina.page.totalElements);
          this.cargando.set(false);
        },
        error: (err) => {
          this.mensajeError.set(err?.error?.mensaje || 'Error al buscar proveedores');
          this.cargando.set(false);
        },
      });
    } else {
      // Carga inicial o campo vacío: usa el endpoint de lista directa sin invocar búsqueda ni filtros backend
      this.proveedorService.listarTodos().subscribe({
        next: (lista: Proveedor[]) => {
          const filtro = this.filtroActivo();
          const filtrados = filtro === null
            ? lista
            : lista.filter((p) => p.activo === filtro);

          const pageSize = 15;
          const totalElem = filtrados.length;
          const totalPages = Math.ceil(totalElem / pageSize) || 1;
          const page = Math.min(this.paginaActual(), totalPages - 1);
          const startIndex = Math.max(0, page * pageSize);
          const pageContent = filtrados.slice(startIndex, startIndex + pageSize);

          this.proveedores.set(pageContent);
          this.paginaActual.set(page);
          this.totalPaginas.set(totalPages);
          this.totalElementos.set(totalElem);
          this.cargando.set(false);
        },
        error: (err) => {
          this.mensajeError.set(err?.error?.mensaje || 'Error al consultar proveedores');
          this.cargando.set(false);
        },
      });
    }
  }

  onTerminoChange(event: Event): void {
    const valor = (event.target as HTMLInputElement).value;
    this.terminoBusqueda.set(valor);
    this.paginaActual.set(0);
    this.cargarProveedores();
  }

  onFiltroActivoChange(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value;
    if (valor === 'activos') {
      this.filtroActivo.set(true);
    } else if (valor === 'inactivos') {
      this.filtroActivo.set(false);
    } else {
      this.filtroActivo.set(null);
    }
    this.paginaActual.set(0);
    this.cargarProveedores();
  }

  cambiarPagina(nuevaPagina: number): void {
    this.paginaActual.set(nuevaPagina);
    this.cargarProveedores();
  }

  abrirModalCrear(): void {
    this.proveedorEnEdicion.set(null);
    this.form.reset({ porcentajeComision: 0 });
    this.modalAbierto.set(true);
  }

  abrirModalEditar(proveedor: Proveedor): void {
    this.proveedorEnEdicion.set(proveedor);
    this.form.patchValue({
      nombre: proveedor.nombre,
      rfc: proveedor.rfc || '',
      porcentajeComision: proveedor.porcentajeComision,
      telefono: proveedor.telefono || '',
      contacto: proveedor.contacto || '',
      email: proveedor.email || '',
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.proveedorEnEdicion.set(null);
    this.form.reset({ porcentajeComision: 0 });
  }

  guardarProveedor(): void {
    if (this.form.invalid) return;

    this.guardando.set(true);
    this.mensajeError.set(null);

    const val = this.form.value;
    const datos = {
      nombre: val.nombre!.trim(),
      rfc: val.rfc ? val.rfc.trim().toUpperCase() : null,
      porcentajeComision: Number(val.porcentajeComision ?? 0),
      telefono: val.telefono ? val.telefono.trim() : null,
      contacto: val.contacto ? val.contacto.trim() : null,
      email: val.email ? val.email.trim() : null,
    };

    const edicion = this.proveedorEnEdicion();
    if (edicion) {
      this.proveedorService.actualizar(edicion.id, datos).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.mensajeExito.set('Proveedor actualizado exitosamente');
          this.cargarProveedores();
        },
        error: (err) => {
          this.guardando.set(false);
          this.mensajeError.set(err?.error?.mensaje || 'Error al actualizar proveedor');
        },
      });
    } else {
      this.proveedorService.crear(datos).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.mensajeExito.set('Proveedor registrado exitosamente');
          this.cargarProveedores();
        },
        error: (err) => {
          this.guardando.set(false);
          this.mensajeError.set(err?.error?.mensaje || 'Error al registrar proveedor');
        },
      });
    }
  }

  iniciarDesactivacion(proveedor: Proveedor): void {
    this.proveedorParaDesactivar.set(proveedor);
    this.proveedorService.contarProductosAsignados(proveedor.id).subscribe({
      next: (conteo) => {
        this.conteoProductosAfectados.set(conteo);
        this.modalDesactivacion.set(true);
      },
      error: () => {
        this.conteoProductosAfectados.set(0);
        this.modalDesactivacion.set(true);
      },
    });
  }

  cancelarDesactivacion(): void {
    this.modalDesactivacion.set(false);
    this.proveedorParaDesactivar.set(null);
  }

  ejecutarDesactivacion(): void {
    const prov = this.proveedorParaDesactivar();
    if (!prov) return;

    this.guardando.set(true);
    this.proveedorService.desactivar(prov.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cancelarDesactivacion();
        this.mensajeExito.set(`Proveedor "${prov.nombre}" desactivado. Sus productos fueron reasignados a PENDIENTE.`);
        this.cargarProveedores();
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(err?.error?.mensaje || 'Error al desactivar proveedor');
      },
    });
  }
}
