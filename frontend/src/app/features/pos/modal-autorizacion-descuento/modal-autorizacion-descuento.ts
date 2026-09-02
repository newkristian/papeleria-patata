import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { AutorizacionManualLinea } from '../../../core/models/carrito.model';
import { AutorizacionDescuentoResponse } from '../../../core/models/autorizacion-descuento.model';
import { AutorizacionDescuentoService } from '../../../core/services/autorizacion-descuento.service';

type Fase = 'formulario' | 'revision';

/**
 * Modal de reautenticación para descuento manual (T6/T7). Nunca calcula ni concede
 * permisos localmente: solo recolecta usuario/contraseña/porcentaje/motivo, llama al
 * endpoint dedicado, y muestra lo que el backend devuelve. La contraseña se limpia
 * del estado del componente inmediatamente después de cada intento (éxito o error) y
 * nunca se autocompleta ni se registra en logs.
 */
@Component({
  selector: 'app-modal-autorizacion-descuento',
  imports: [DecimalPipe],
  templateUrl: './modal-autorizacion-descuento.html',
})
export class ModalAutorizacionDescuentoComponent {
  private readonly autorizacionService = inject(AutorizacionDescuentoService);

  readonly productoId = input.required<number>();
  readonly productoNombre = input.required<string>();
  readonly cantidad = input.required<number>();
  readonly carritoId = input.required<string>();

  /** Se emite solo cuando el cajero decide aplicar la autorización ya obtenida a la línea. */
  readonly autorizada = output<AutorizacionManualLinea>();
  /** Se emite al cerrar el modal por cualquier vía (cancelar, descartar o aplicar). */
  readonly cerrado = output<void>();

  readonly fase = signal<Fase>('formulario');
  readonly username = signal('');
  readonly password = signal('');
  readonly porcentaje = signal<number | null>(null);
  readonly motivo = signal('');
  readonly enviando = signal(false);
  readonly error = signal<string | null>(null);
  readonly resultado = signal<AutorizacionDescuentoResponse | null>(null);

  readonly formularioValido = computed(() => {
    const porcentaje = this.porcentaje();
    return (
      this.username().trim() !== '' &&
      this.password() !== '' &&
      porcentaje !== null &&
      porcentaje >= 0 &&
      porcentaje <= 30 &&
      this.motivo().trim() !== ''
    );
  });

  onUsernameInput(valor: string): void {
    this.username.set(valor);
  }

  onPasswordInput(valor: string): void {
    this.password.set(valor);
  }

  onPorcentajeInput(valor: string): void {
    this.porcentaje.set(valor === '' ? null : Number(valor));
  }

  onMotivoInput(valor: string): void {
    this.motivo.set(valor);
  }

  solicitar(): void {
    const porcentaje = this.porcentaje();
    if (this.enviando() || !this.formularioValido() || porcentaje === null) {
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    const solicitud = {
      username: this.username().trim(),
      password: this.password(),
      productoId: this.productoId(),
      cantidad: this.cantidad(),
      porcentaje,
      motivo: this.motivo().trim(),
      carritoId: this.carritoId(),
    };
    // La contraseña no debe sobrevivir más allá de este punto: se limpia del estado
    // del componente ya con la solicitud armada, antes de esperar la respuesta.
    this.password.set('');

    this.autorizacionService.solicitar(solicitud).subscribe({
      next: (respuesta) => {
        this.enviando.set(false);
        this.resultado.set(respuesta);
        this.fase.set('revision');
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.error.set(this.mensajeDeError(err));
      },
    });
  }

  aplicar(): void {
    const resultado = this.resultado();
    if (!resultado) {
      return;
    }
    this.autorizada.emit({
      referencia: resultado.referencia,
      porcentaje: resultado.porcentaje,
      expiraEn: resultado.expiraEn,
      motivo: this.motivo(),
    });
    this.cerrarYLimpiar();
  }

  /**
   * La autorización ya fue emitida por el backend, pero al no aplicarla aquí
   * simplemente queda sin usar y expira sola a los 2 minutos — no existe un endpoint
   * de cancelación explícita.
   */
  descartar(): void {
    this.cerrarYLimpiar();
  }

  cancelar(): void {
    this.cerrarYLimpiar();
  }

  private cerrarYLimpiar(): void {
    this.username.set('');
    this.password.set('');
    this.porcentaje.set(null);
    this.motivo.set('');
    this.error.set(null);
    this.resultado.set(null);
    this.fase.set('formulario');
    this.cerrado.emit();
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.';
    }
    const mensaje = (err.error as { mensaje?: unknown } | null)?.mensaje;
    if (typeof mensaje === 'string' && mensaje.trim()) {
      return mensaje;
    }
    if (err.status === 401 || err.status === 403) {
      return 'Tu sesión expiró o no tienes permisos para esta operación. Vuelve a iniciar sesión.';
    }
    return 'No se pudo solicitar la autorización. Intenta de nuevo.';
  }
}
