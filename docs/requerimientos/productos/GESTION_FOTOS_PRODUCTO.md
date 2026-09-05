# Gestión de fotografías de producto

**Estado:** Implementado y verificado  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Subir, consultar, descargar, eliminar y seleccionar la fotografía principal de un
producto mediante un procesamiento asíncrono, incluyendo una imagen normalizada y
miniaturas seguras.

## Alcance aprobado

- Cada solicitud admitirá como máximo 4 MB, tanto en configuración multipart como en
  validación de aplicación.
- Solo se aceptarán imágenes raster JPEG y PNG verificadas por firma y decodificación.
  No se aceptarán SVG, GIF animado, WebP ni formatos no soportados expresamente.
- No se confiará en el nombre, extensión o `Content-Type` enviados por el cliente.
- Antes de decodificar por completo se comprobarán formato, dimensiones y cantidad
  total de píxeles para reducir riesgo de bombas de descompresión y agotamiento de
  memoria.
- La imagen se reorientará cuando corresponda, se recodificará sin metadatos y se
  reducirá manteniendo proporción a un máximo de 512 × 512 px.
- Se generará una miniatura exacta de 80 × 80 px mediante recorte centrado, sin
  deformación.
- El archivo recibido se guardará primero en un área temporal no pública con un nombre
  generado por el servidor.
- El procesamiento se ejecutará de forma asíncrona en un executor con pool y cola
  acotados. El endpoint devolverá 202 con identificador y estado.
- Los estados serán `PENDIENTE`, `PROCESANDO`, `LISTA` y `ERROR`.
- Solo una fotografía `LISTA` podrá descargarse como imagen de catálogo o marcarse
  como principal.
- El proceso será idempotente, limpiará temporales y archivos parciales, y permitirá
  eliminar o reintentar un registro fallido.

## Autorización aprobada

- `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA`: cargar, consultar estados, reintentar,
  ordenar, marcar principal y eliminar fotografías.
- `VENDEDOR`: descargar únicamente fotografías `LISTA` necesarias para productos
  activos del POS.
- El backend verificará producto y fotografía en cada operación para evitar IDOR y
  nunca resolverá rutas proporcionadas por el cliente.

## Implementación verificada

- Pipeline asíncrono seguro implementado en `ProductoFotoProcessor` con
  `ThreadPoolTaskExecutor` de hilos y cola acotados.
- Subida multipart que responde inmediatamente `202 ACCEPTED` con DTO en estado `PENDIENTE`.
- Estados persistentes (`PENDIENTE`, `PROCESANDO`, `LISTA`, `ERROR` con mensaje descriptivo).
- Validación estricta con `ValidadorSeguridadImagen`: comprobación de magic bytes y
  decodificación real (`BufferedImage`), rechazando SVG ejecutable, WebP, GIFs y
  archivos hostiles o truncados sin dejar archivos huérfanos.
- Normalización a 512 × 512 px, generación de miniatura 80 × 80 px y remoción de metadatos EXIF.
- Eliminación física de archivos en disco al borrar la fotografía y marcado de foto principal.
- Modal interactivo en frontend (`ProductoFotosModalComponent`, `ProductoFotoService`)
  con drag & drop, validación cliente de 4 MB y tipo MIME, barra de progreso,
  polling acotado (1.5s, máximo 15 intentos) con detención automática y recarga reactiva
  en catálogo de productos.
- Pruebas unitarias de validador y procesador, y prueba de integración E2E en
  `FlujoIntegralCargaProductosIntegrationTest`.

## Cierre de pendientes del hito

- **Pipeline asíncrono y executor:** Implementado en Tarea 6.
- **Validación de seguridad y archivos hostiles:** Implementado en Tarea 6.
- **Interfaz y polling en frontend:** Implementado en Tarea 11.

## Dependencia no bloqueante

- Un executor en memoria no recupera automáticamente trabajos interrumpidos por un
  reinicio del backend. Antes de producción deberá decidirse si al arrancar se
  reconcilian registros `PENDIENTE`/`PROCESANDO` o si se incorpora una cola durable.
  El pipeline inicial puede implementarse con reconciliación simple sin bloquear el
  resto del mantenimiento.

## Criterios de aceptación

- Un archivo de 4 MB exactos puede aceptarse y uno mayor produce 413.
- JPEG y PNG válidos generan una imagen de máximo 512 × 512 y una miniatura 80 × 80.
- Contenido falsificado, archivo vacío/truncado, formato prohibido o dimensiones
  excesivas se rechaza sin dejar archivos huérfanos.
- La solicitud devuelve 202 sin esperar el redimensionado y el estado puede
  consultarse hasta `LISTA` o `ERROR`.
- Saturar la cola no crea hilos ilimitados ni deja registros permanentemente
  inconsistentes.
- Solo roles autorizados pueden modificar fotografías y ningún usuario puede acceder
  a una ruta física o a una fotografía de otro producto fabricando IDs.
