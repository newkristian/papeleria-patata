import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { switchMap, take } from 'rxjs/operators';
import { ProductoFotoService } from '../../../core/services/producto-foto.service';
import { ProductoFoto } from '../../../core/models/producto-foto.model';

@Component({
  selector: 'app-producto-fotos-modal',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-xl border border-gray-200 w-full max-w-3xl p-6 space-y-6 animate-in fade-in zoom-in-95 my-8">
        <!-- Encabezado -->
        <div class="flex items-center justify-between border-b border-gray-100 pb-4">
          <div>
            <h3 class="text-lg font-bold text-gray-900">
              Galería de Fotografías
            </h3>
            <p class="text-xs text-gray-500 mt-0.5">
              Producto: <span class="font-semibold text-gray-800">{{ productoNombre }}</span> (ID: {{ productoId }})
            </p>
          </div>
          <button
            type="button"
            (click)="onCerrar()"
            class="text-gray-400 hover:text-gray-600 text-2xl font-bold leading-none">
            &times;
          </button>
        </div>

        <!-- Alertas -->
        @if (mensajeError()) {
          <div class="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center justify-between">
            <span>{{ mensajeError() }}</span>
            <button type="button" (click)="mensajeError.set(null)" class="text-rose-600 font-bold ml-2">&times;</button>
          </div>
        }
        @if (mensajeExito()) {
          <div class="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs flex items-center justify-between">
            <span>{{ mensajeExito() }}</span>
            <button type="button" (click)="mensajeExito.set(null)" class="text-emerald-600 font-bold ml-2">&times;</button>
          </div>
        }

        <!-- Zona de Carga de Imagen -->
        <div class="p-5 border-2 border-dashed border-gray-200 rounded-2xl bg-gray-50/75 text-center space-y-3">
          <div class="w-10 h-10 mx-auto rounded-full bg-blue-50 text-blue-600 flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <div>
            <label class="cursor-pointer inline-flex items-center px-4 py-2 bg-white border border-gray-300 rounded-xl text-xs font-semibold text-gray-700 hover:bg-gray-50 shadow-sm transition-colors">
              <span>Seleccionar fotografía</span>
              <input
                type="file"
                accept="image/jpeg,image/png"
                (change)="onArchivoSeleccionado($event)"
                class="sr-only" />
            </label>
            <p class="text-[11px] text-gray-400 mt-1.5">
              JPG o PNG hasta 4 MB exactos. Se redimensiona automáticamente a 512×512 y miniatura 80×80.
            </p>
          </div>

          @if (archivoSeleccionado()) {
            <div class="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3">
              <span class="text-xs text-gray-700 font-medium truncate max-w-xs">
                📄 {{ archivoSeleccionado()?.name }} ({{ ((archivoSeleccionado()?.size ?? 0) / 1024).toFixed(1) }} KB)
              </span>
              <label class="inline-flex items-center text-xs text-gray-600 space-x-1.5 cursor-pointer">
                <input
                  type="checkbox"
                  [(ngModel)]="marcarComoPrincipal"
                  class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                <span>Marcar como principal</span>
              </label>
              <button
                type="button"
                [disabled]="subiendo()"
                (click)="subirArchivo()"
                class="px-3.5 py-1.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white rounded-lg text-xs font-semibold transition-colors flex items-center">
                @if (subiendo()) {
                  <div class="inline-block animate-spin rounded-full h-3.5 w-3.5 border-b-2 border-white mr-1.5"></div>
                }
                Subir Foto
              </button>
            </div>
          }
        </div>

        <!-- Galería de Fotos Existentes -->
        <div class="space-y-3">
          <div class="flex items-center justify-between text-xs text-gray-500">
            <span class="font-bold uppercase tracking-wider text-gray-600">Fotografías del producto ({{ fotos().length }})</span>
            @if (hayFotosEnProceso()) {
              <span class="flex items-center text-blue-600 font-medium">
                <div class="inline-block animate-spin rounded-full h-3 w-3 border-b-2 border-blue-600 mr-1.5"></div>
                Procesando en segundo plano...
              </span>
            }
          </div>

          @if (cargandoFotos()) {
            <div class="p-8 text-center text-gray-400 text-xs">Cargando fotos...</div>
          } @else if (fotos().length === 0) {
            <div class="p-8 text-center bg-gray-50 rounded-xl text-xs text-gray-400">
              No hay fotografías cargadas para este producto todavía.
            </div>
          } @else {
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
              @for (foto of fotos(); track foto.id) {
                <div class="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm flex flex-col relative group">
                  <!-- Imagen o preview -->
                  <div class="h-36 bg-gray-100 relative flex items-center justify-center overflow-hidden">
                    @if (foto.estadoProcesamiento === 'COMPLETADO') {
                      <img [src]="foto.urlThumbnail || foto.urlOriginal" [alt]="foto.nombreArchivo" class="w-full h-full object-cover" />
                    } @else if (foto.estadoProcesamiento === 'ERROR') {
                      <div class="text-center p-3 text-rose-600">
                        <svg class="w-8 h-8 mx-auto mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <span class="text-[10px] font-bold block">Fallo de procesamiento</span>
                      </div>
                    } @else {
                      <div class="text-center p-3 text-blue-600">
                        <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mb-1.5"></div>
                        <span class="text-[10px] font-medium block">Procesando imagen...</span>
                      </div>
                    }

                    <!-- Badge de Foto Principal -->
                    @if (foto.esPrincipal) {
                      <span class="absolute top-2 left-2 px-2 py-0.5 rounded-md text-[10px] font-bold bg-amber-500 text-white shadow-sm">
                        ★ Principal
                      </span>
                    }
                  </div>

                  <!-- Detalles y Acciones -->
                  <div class="p-2.5 flex-1 flex flex-col justify-between space-y-2 text-xs">
                    <div>
                      <div class="font-medium text-gray-800 truncate" [title]="foto.nombreArchivo">
                        {{ foto.nombreArchivo }}
                      </div>
                      <div class="text-[10px] text-gray-400 mt-0.5">
                        @if (foto.estadoProcesamiento === 'COMPLETADO') {
                          <span class="text-emerald-600 font-medium">✓ Lista</span>
                        } @else if (foto.estadoProcesamiento === 'ERROR') {
                          <span class="text-rose-600 font-medium" [title]="foto.mensajeError || 'Error'">
                            ✕ {{ foto.mensajeError || 'Error en proceso' }}
                          </span>
                        } @else {
                          <span class="text-blue-600 font-medium">⏳ {{ foto.estadoProcesamiento }}</span>
                        }
                      </div>
                    </div>

                    <!-- Botones de Acción -->
                    <div class="flex items-center justify-between pt-1 border-t border-gray-100">
                      @if (!foto.esPrincipal && foto.estadoProcesamiento === 'COMPLETADO') {
                        <button
                          type="button"
                          (click)="establecerComoPrincipal(foto.id)"
                          class="text-[11px] text-blue-600 hover:text-blue-800 font-medium">
                          Hacer principal
                        </button>
                      } @else if (foto.estadoProcesamiento === 'ERROR') {
                        <button
                          type="button"
                          (click)="reintentarFoto(foto.id)"
                          class="text-[11px] text-amber-600 hover:text-amber-800 font-medium">
                          Reintentar
                        </button>
                      } @else {
                        <span></span>
                      }

                      <button
                        type="button"
                        (click)="eliminarFoto(foto.id)"
                        class="text-gray-400 hover:text-rose-600 p-1 rounded transition-colors"
                        title="Eliminar fotografía">
                        <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              }
            </div>
          }
        </div>

        <!-- Pie de modal -->
        <div class="flex justify-end pt-3 border-t border-gray-100">
          <button
            type="button"
            (click)="onCerrar()"
            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-semibold transition-colors">
            Cerrar Galería
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ProductoFotosModalComponent implements OnInit, OnDestroy {
  @Input({ required: true }) productoId!: number;
  @Input({ required: true }) productoNombre!: string;
  @Output() cerrar = new EventEmitter<void>();
  @Output() fotosActualizadas = new EventEmitter<void>();

  private readonly fotoService = inject(ProductoFotoService);

  readonly fotos = signal<ProductoFoto[]>([]);
  readonly cargandoFotos = signal(false);
  readonly subiendo = signal(false);
  readonly archivoSeleccionado = signal<File | null>(null);
  marcarComoPrincipal = false;

  readonly mensajeExito = signal<string | null>(null);
  readonly mensajeError = signal<string | null>(null);

  private pollingSub?: Subscription;

  ngOnInit(): void {
    this.cargarFotos();
  }

  ngOnDestroy(): void {
    this.detenerPolling();
  }

  cargarFotos(): void {
    this.cargandoFotos.set(true);
    this.fotoService.listarFotos(this.productoId).subscribe({
      next: (lista) => {
        this.fotos.set(lista);
        this.cargandoFotos.set(false);
        this.evaluarPolling(lista);
      },
      error: () => {
        this.cargandoFotos.set(false);
      },
    });
  }

  onArchivoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.mensajeError.set(null);

    // Validación de formato: solo jpeg y png
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      this.mensajeError.set('Formato no permitido. Solo se aceptan imágenes JPG o PNG.');
      input.value = '';
      return;
    }

    // Validación de tamaño exacto: máximo 4 MB (4 * 1024 * 1024 bytes)
    const MAX_BYTES = 4 * 1024 * 1024;
    if (file.size > MAX_BYTES) {
      this.mensajeError.set('El archivo excede el tamaño máximo permitido de 4 MB.');
      input.value = '';
      return;
    }

    this.archivoSeleccionado.set(file);
  }

  subirArchivo(): void {
    const file = this.archivoSeleccionado();
    if (!file) return;

    this.subiendo.set(true);
    this.mensajeError.set(null);

    this.fotoService.subirFoto(this.productoId, file, this.marcarComoPrincipal).subscribe({
      next: () => {
        this.subiendo.set(false);
        this.archivoSeleccionado.set(null);
        this.marcarComoPrincipal = false;
        this.mensajeExito.set('Fotografía subida. El sistema la está procesando en segundo plano.');
        this.cargarFotos();
        this.fotosActualizadas.emit();
      },
      error: (err) => {
        this.subiendo.set(false);
        this.mensajeError.set(err?.error?.mensaje || 'Error al subir la fotografía.');
      },
    });
  }

  establecerComoPrincipal(fotoId: number): void {
    this.fotoService.establecerPrincipal(this.productoId, fotoId).subscribe({
      next: () => {
        this.mensajeExito.set('Foto establecida como principal.');
        this.cargarFotos();
        this.fotosActualizadas.emit();
      },
      error: (err) => {
        this.mensajeError.set(err?.error?.mensaje || 'Error al establecer foto principal.');
      },
    });
  }

  eliminarFoto(fotoId: number): void {
    this.fotoService.eliminarFoto(this.productoId, fotoId).subscribe({
      next: () => {
        this.mensajeExito.set('Fotografía eliminada.');
        this.cargarFotos();
        this.fotosActualizadas.emit();
      },
      error: (err) => {
        this.mensajeError.set(err?.error?.mensaje || 'Error al eliminar la fotografía.');
      },
    });
  }

  reintentarFoto(fotoId: number): void {
    this.fotoService.reintentarFoto(this.productoId, fotoId).subscribe({
      next: () => {
        this.mensajeExito.set('Reintentando procesamiento...');
        this.cargarFotos();
      },
      error: (err) => {
        this.mensajeError.set(err?.error?.mensaje || 'Error al reintentar procesamiento.');
      },
    });
  }

  hayFotosEnProceso(): boolean {
    return this.fotos().some(
      (f) => f.estadoProcesamiento === 'PENDIENTE' || f.estadoProcesamiento === 'PROCESANDO'
    );
  }

  private evaluarPolling(lista: ProductoFoto[]): void {
    const pendientes = lista.some(
      (f) => f.estadoProcesamiento === 'PENDIENTE' || f.estadoProcesamiento === 'PROCESANDO'
    );

    if (pendientes) {
      this.iniciarPolling();
    } else {
      this.detenerPolling();
    }
  }

  private iniciarPolling(): void {
    if (this.pollingSub && !this.pollingSub.closed) return;

    // Polling acotado: cada 1.5s, máximo 15 intentos
    this.pollingSub = interval(1500)
      .pipe(
        take(15),
        switchMap(() => this.fotoService.listarFotos(this.productoId))
      )
      .subscribe({
        next: (lista) => {
          this.fotos.set(lista);
          const aunPendientes = lista.some(
            (f) => f.estadoProcesamiento === 'PENDIENTE' || f.estadoProcesamiento === 'PROCESANDO'
          );
          if (!aunPendientes) {
            this.detenerPolling();
            this.fotosActualizadas.emit();
          }
        },
        error: () => {
          this.detenerPolling();
        },
      });
  }

  private detenerPolling(): void {
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
      this.pollingSub = undefined;
    }
  }

  onCerrar(): void {
    this.detenerPolling();
    this.cerrar.emit();
  }
}
