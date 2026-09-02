import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { DetalleVentaRequest, MetodoPago, VentaResponse } from '../../../core/models/venta.model';
import { CarritoService } from '../../../core/services/carrito.service';
import { VentaService } from '../../../core/services/venta.service';

const METODOS_PAGO: { valor: MetodoPago; etiqueta: string }[] = [
  { valor: 'EFECTIVO', etiqueta: 'Efectivo' },
  { valor: 'TARJETA_DEBITO', etiqueta: 'Tarjeta de débito' },
  { valor: 'TARJETA_CREDITO', etiqueta: 'Tarjeta de crédito' },
  { valor: 'TRANSFERENCIA', etiqueta: 'Transferencia' },
];

const ETIQUETAS_DESCUENTO: Record<string, string> = {
  NINGUNO: 'Sin descuento',
  CANTIDAD: 'Promoción por cantidad',
  CLIENTE: 'Promoción de cliente',
  MANUAL: 'Descuento manual autorizado',
};

/**
 * Confirma la venta contra POST /api/v1/ventas y muestra el resultado real que
 * devuelve el servidor (nunca calcula ni asume precios, promociones ni el total).
 * El monto recibido y el cambio son un cálculo puramente informativo del frontend —
 * la entidad Venta del backend no tiene esos campos.
 */
@Component({
  selector: 'app-cobro',
  imports: [DecimalPipe],
  templateUrl: './cobro.html',
})
export class CobroComponent {
  private readonly ventaService = inject(VentaService);

  readonly carrito = inject(CarritoService);
  readonly metodosPago = METODOS_PAGO;
  readonly etiquetasDescuento = ETIQUETAS_DESCUENTO;

  readonly metodoPago = signal<MetodoPago>('EFECTIVO');
  readonly montoRecibido = signal<number | null>(null);
  readonly enviando = signal(false);
  readonly error = signal<string | null>(null);
  readonly ventaConfirmada = signal<VentaResponse | null>(null);

  /** Contra el subtotal ESTIMADO del carrito — puede diferir del cambio real tras cobrar. */
  readonly cambioEstimado = computed<number | null>(() => {
    const monto = this.montoRecibido();
    if (this.metodoPago() !== 'EFECTIVO' || monto === null) {
      return null;
    }
    return monto - this.carrito.subtotalEstimado();
  });

  /** Contra el total DEFINITIVO devuelto por el servidor, una vez confirmada la venta. */
  readonly cambioReal = computed<number | null>(() => {
    const venta = this.ventaConfirmada();
    const monto = this.montoRecibido();
    if (!venta || venta.metodoPago !== 'EFECTIVO' || monto === null) {
      return null;
    }
    return monto - venta.total;
  });

  onMetodoPagoChange(valor: string): void {
    this.metodoPago.set(valor as MetodoPago);
  }

  onMontoRecibidoInput(valor: string): void {
    const monto = Number(valor);
    this.montoRecibido.set(valor === '' || Number.isNaN(monto) ? null : monto);
  }

  cobrar(): void {
    // Doble guarda contra envíos duplicados: el botón ya se deshabilita mientras
    // `enviando()` es true, pero un doble clic muy rápido podría llegar antes de que
    // el binding se actualice.
    if (this.enviando() || this.carrito.lineas().length === 0) {
      return;
    }

    const detalles: DetalleVentaRequest[] = this.carrito.lineas().map((linea) => ({
      productoId: linea.productoId,
      cantidad: linea.cantidad,
      ...(linea.autorizacionManual ? { autorizacionDescuento: linea.autorizacionManual.referencia } : {}),
    }));

    this.enviando.set(true);
    this.error.set(null);

    this.ventaService
      .crear({
        clienteId: this.carrito.clienteSeleccionado()?.id ?? null,
        metodoPago: this.metodoPago(),
        detalles,
        carritoId: this.carrito.carritoId(),
      })
      .subscribe({
        next: (venta) => {
          this.enviando.set(false);
          this.ventaConfirmada.set(venta);
        },
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          this.error.set(this.mensajeDeError(err));
        },
      });
  }

  nuevaVenta(): void {
    this.carrito.vaciar();
    this.ventaConfirmada.set(null);
    this.metodoPago.set('EFECTIVO');
    this.montoRecibido.set(null);
    this.error.set(null);
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.';
    }
    // Errores manejados por GlobalExceptionHandler (stock, validación, rol con sesión
    // válida, etc.) sí traen `mensaje`. Un 401/403 sin token — el filtro de seguridad
    // los rechaza antes de llegar al controlador — no trae cuerpo JSON: se distingue
    // aquí, no se asume que todo 401/403 significa "sesión expirada".
    const mensaje = (err.error as { mensaje?: unknown } | null)?.mensaje;
    if (typeof mensaje === 'string' && mensaje.trim()) {
      return mensaje;
    }
    if (err.status === 401 || err.status === 403) {
      return 'Tu sesión expiró o no tienes permisos para esta operación. Vuelve a iniciar sesión.';
    }
    return 'No se pudo confirmar la venta. Intenta de nuevo.';
  }
}
