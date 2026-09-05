// Modelos para la gestión de fotografías de productos y pipeline asíncrono.

export type EstadoProcesamientoFoto = 'PENDIENTE' | 'PROCESANDO' | 'COMPLETADO' | 'ERROR';

export interface ProductoFoto {
  id: number;
  nombreArchivo: string;
  contentType: string;
  tamanio: number;
  esPrincipal: boolean;
  orden: number;
  fechaSubida: string;
  urlOriginal: string;
  urlThumbnail: string;
  estadoProcesamiento: EstadoProcesamientoFoto;
  mensajeError: string | null;
}
