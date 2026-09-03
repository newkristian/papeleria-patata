import { DecimalPipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, inject, output, signal, viewChild } from '@angular/core';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { ProductoListado } from '../../../core/models/producto.model';
import { ProductoService } from '../../../core/services/producto.service';

type EstadoBusqueda = 'inicial' | 'cargando' | 'listo' | 'vacio' | 'error';

const TAMANO_PAGINA = 20;
const DEBOUNCE_MS = 300;

/**
 * Input único pensado tanto para lector de código de barras (Enter, búsqueda exacta)
 * como para tecleo libre (búsqueda por nombre con debounce). Solo busca y selecciona;
 * no conoce el carrito — emite `productoSeleccionado` y quien lo escuche decide qué
 * hacer (Sub-tarea 3).
 */
@Component({
  selector: 'app-buscador-productos',
  imports: [DecimalPipe],
  templateUrl: './buscador-productos.html',
})
export class BuscadorProductosComponent implements OnDestroy {
  private readonly productoService = inject(ProductoService);
  private readonly terminoIngresado = new Subject<string>();
  private readonly subscripcion = this.terminoIngresado
    .pipe(debounceTime(DEBOUNCE_MS), distinctUntilChanged())
    .subscribe((termino) => this.ejecutarBusqueda(termino));

  private readonly inputBusqueda = viewChild<ElementRef<HTMLInputElement>>('inputBusqueda');

  readonly productoSeleccionado = output<ProductoListado>();

  readonly termino = signal('');
  readonly estado = signal<EstadoBusqueda>('inicial');
  readonly resultados = signal<ProductoListado[]>([]);
  readonly errorMensaje = signal<string | null>(null);

  ngOnDestroy(): void {
    this.subscripcion.unsubscribe();
  }

  onInput(valor: string): void {
    this.termino.set(valor);
    this.terminoIngresado.next(valor);
  }

  /**
   * Un lector de código de barras escribe rápido y termina con Enter. Se intenta
   * primero como código exacto; si no coincide con ninguno no es un error real, la
   * búsqueda por término ya en curso (o la que dispare el debounce) sigue vigente.
   */
  onEnter(): void {
    const codigo = this.termino().trim();
    if (!codigo) {
      return;
    }
    this.productoService.buscarPorCodigoBarras(codigo).subscribe({
      next: (producto) => this.seleccionar(producto),
      error: () => {
        /* no es un código de barras válido: se ignora, no es un error de búsqueda */
      },
    });
  }

  seleccionar(producto: ProductoListado): void {
    if (this.agotado(producto)) {
      return;
    }
    this.productoSeleccionado.emit(producto);
    this.termino.set('');
    this.resultados.set([]);
    this.estado.set('inicial');
    this.inputBusqueda()?.nativeElement.focus();
  }

  reintentar(): void {
    this.ejecutarBusqueda(this.termino());
  }

  agotado(producto: ProductoListado): boolean {
    return !producto.cantidadDesconocida && producto.stockActual <= 0;
  }

  private ejecutarBusqueda(termino: string): void {
    const valor = termino.trim();
    if (!valor) {
      this.estado.set('inicial');
      this.resultados.set([]);
      this.errorMensaje.set(null);
      return;
    }

    this.estado.set('cargando');
    this.errorMensaje.set(null);
    this.productoService.buscar(valor, 0, TAMANO_PAGINA).subscribe({
      next: (pagina) => {
        this.resultados.set(pagina.content);
        this.estado.set(pagina.content.length === 0 ? 'vacio' : 'listo');
      },
      error: () => {
        this.estado.set('error');
        this.errorMensaje.set('No se pudo buscar productos. Intenta de nuevo.');
      },
    });
  }
}
