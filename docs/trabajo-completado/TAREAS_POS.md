# Interfaz POS del frontend — re-alcance de la Tarea 7

**Estado del plan:** Completado — Sub-tareas 1 a 4 (4a y 4b) implementadas y verificadas
**Última actualización:** 2 de septiembre de 2026

## Propósito

La Tarea 7 de `docs/trabajo-completado/TAREAS_VENTAS.md` ("Interfaz POS para promociones
y descuento manual") describe únicamente la capa de promociones y descuento manual del
POS, asumiendo que ya existe una pantalla de POS con buscador, carrito y cobro
funcionando. Al revisar el frontend real (`frontend/src/app`) se confirmó que no existe
nada de eso: solo hay landing pública, login y un placeholder en la ruta `/pos`.

Esto coincide con cuatro requerimientos ya aprobados en `docs/requerimientos/pos/` que
documentan exactamente esa ausencia ("No existe implementación frontend"). Se acordó
con el propietario tratar la Tarea 7 como **cuatro sub-tareas en orden**, cada una con
su propio análisis, implementación y verificación, en vez de un solo paso.

Este documento reemplaza a `docs/planeacion_tareas.md` como fuente de seguimiento del
frontend del POS — ese archivo se movió a `docs/legacy/` y ya no es fuente vigente
(regla 4 de `docs/requerimientos/README.md`). `frontend/CLAUDE.md` todavía lo referencia
como si estuviera en la raíz; queda pendiente corregir ese puntero (ver "Pendientes no
bloqueantes").

## Sub-tareas

### Sub-tarea 1 — Layout base del POS

**Requerimiento:** `docs/requerimientos/pos/INTERFAZ_POS.md`
**Estado:** Completado (2 de septiembre de 2026)

Implementado `PosLayoutComponent` (`frontend/src/app/features/pos/pos-layout/`):
barra superior propia (marca + "Cerrar sesión" vía `AuthService.logout()`, ya
existente) y área principal en grid de dos columnas (`1fr` / `380px` desde `lg`),
apiladas en pantallas angostas. Ruta `/pos` (`app.routes.ts`) reemplaza el placeholder
que cargaba `HomeComponent` por este layout, manteniendo `authGuard`. Los paneles de
buscador (`<section>`) y carrito (`<aside>`) quedan como placeholders con su
`aria-label` ya puesto, listos para que las sub-tareas 2–4 inserten los componentes
reales sin reestructurar el shell.

Verificado con `ng build` (compila limpio, `pos-layout` aparece como chunk lazy
propio) y `ng test` (sin nuevas fallas; la única prueba roja —
`app.spec.ts > should render title`— es el scaffold por defecto del CLI, preexistente
y no tocado por esta sub-tarea).

### Sub-tarea 2 — Búsqueda de productos

**Requerimiento:** `docs/requerimientos/pos/BUSQUEDA_PRODUCTOS_POS.md`
**Estado:** Completado (2 de septiembre de 2026)

Implementado `ProductoService` (`core/services/producto.service.ts`, con
`producto.service.spec.ts`): `buscar(termino, page, size)` contra `GET
/productos/buscar` y `buscarPorCodigoBarras(codigo)` contra `GET
/productos/codigo/{codigoBarras}`. Contrato de paginación (`{ content, page: { size,
number, totalElements, totalPages } }`) verificado contra el backend real vía
`docker compose` antes de escribir los modelos (`core/models/producto.model.ts`), no
asumido.

`BuscadorProductosComponent` (`features/pos/buscador-productos/`): input único con
autofoco; búsqueda por término con debounce de 300ms mientras se escribe; Enter
intenta primero código de barras exacto (flujo de lector), y si no coincide no se
trata como error — la búsqueda por término sigue vigente. Resultados en tarjetas con
nombre, código, categoría, precio y stock (badge "sin control de stock" si
`cantidadDesconocida`, "sin stock"/no seleccionable si el stock conocido es 0). Estados
inicial/cargando/vacío/error con reintento. Al seleccionar, emite `productoSeleccionado`
(output) y limpia el input para seguir escaneando. Sin paginación en esta pasada (tope
de 20 resultados) — simplificación deliberada, el requerimiento no pide paginar
explícitamente.

`PosLayoutComponent` monta el buscador en el panel izquierdo y agrega
`seleccionRecienteStub`, una lista de solo lectura sin cantidades/edición/totales,
únicamente para confirmar visualmente que la selección llega de punta a punta. Marcada
en el código como stub temporal — la Sub-tarea 3 la reemplaza por completo con estado
reactivo real del carrito.

Verificado con `ng build` (limpio, `pos-layout` crece de 1.93 kB a 8.58 kB con el
buscador incluido) y `ng test` (los 3 tests nuevos de `ProductoService` pasan; sin
fallas nuevas, la única roja sigue siendo el scaffold preexistente de `app.spec.ts`).
No se hizo verificación visual en navegador real (sin herramienta de browser
disponible en este entorno); la revisión visual con `ng serve` + `docker compose`
queda para el propietario.

### Sub-tarea 3 — Carrito de venta

**Requerimiento:** `docs/requerimientos/pos/CARRITO_VENTA.md`
**Estado:** Completado (2 de septiembre de 2026)

Implementado `ClienteService` (`core/services/cliente.service.ts`, con spec):
`listar()` contra `GET /clientes` (lista completa, sin paginar ni filtrar en
servidor — el endpoint no lo ofrece; el filtrado por cliente en el selector queda para
una futura mejora si la lista crece demasiado para un `<select>` simple).

`CarritoService` (`core/services/carrito.service.ts`, `providedIn: 'root'`, con spec
de 10 casos): estado 100% con signals — `lineas`, `clienteSeleccionado` (`null` =
mostrador, misma semántica que `clienteId: null` en `POST /api/v1/ventas`),
`subtotalEstimado` y `totalArticulos` computed. `agregarProducto` incrementa cantidad
si el producto ya está en el carrito en vez de duplicar línea (coincide con cómo
`VentaService` consolida por `productoId` en el backend). `establecerCantidad` limita
a 1 como mínimo y al stock conocido como máximo (ayuda de UX, no control real — el
backend siempre revalida). `decrementar` desde 1 quita la línea; `eliminar` la quita
explícitamente sin importar la cantidad.

`CarritoComponent` (`features/pos/carrito/`): selector de cliente (`<select>` nativo,
sin autocompletado propio — no se justifica sobre una lista sin búsqueda server-side),
líneas con controles −/input numérico/+, botón eliminar, aviso cuando la cantidad
alcanza el stock conocido, y total estimado etiquetado explícitamente como tal
("El total definitivo lo calcula el servidor al cobrar").

`PosLayoutComponent`: `onProductoSeleccionado` ahora llama a
`CarritoService.agregarProducto()`; se eliminó el `seleccionRecienteStub` temporal de
la Sub-tarea 2, reemplazado por completo por `<app-carrito>`.

Verificado con `ng build` (limpio; `pos-layout` crece de 8.58 kB a 14.95 kB) y
`ng test` (11 tests nuevos en verde — 1 de `ClienteService`, 10 de `CarritoService`;
sin fallas nuevas, la única roja sigue siendo el scaffold preexistente de
`app.spec.ts`). Sin verificación visual en navegador real, igual que en la Sub-tarea 2.

### Sub-tarea 4 — Cobro (incluye el alcance completo de la Tarea 7)

**Requerimiento:** `docs/requerimientos/pos/COBRO_POS.md`
**Estado:** Dividida en dos pasadas — 4a completada, 4b pendiente

Confirmar la venta contra `POST /api/v1/ventas`, método de pago, cálculo de cambio en
efectivo, prevención de envíos duplicados. Presentar por línea el precio de lista, la
promoción aplicada, el ahorro y el subtotal definitivo que devuelve el backend. Botón
por línea para solicitar descuento manual, que abre el modal de reautenticación
(usuario, contraseña, porcentaje y motivo de un `GERENTE`/`ADMINISTRADOR`) contra
`POST /api/v1/autorizaciones-descuento`. El contrato completo de ese flujo —
`carritoId`, referencia opaca, manejo de expiración/reutilización/errores — está
documentado en `docs/requerimientos/ventas/AUTORIZACION_DESCUENTO_MANUAL.md`, sección
"Contrato para el frontend".

Dado el tamaño real de esta sub-tarea (cobro completo + todo el alcance de la Tarea 7
original), se dividió en dos pasadas verificadas por separado:

#### 4a — Cobro básico (completada, 2 de septiembre de 2026)

Implementado `venta.model.ts` (`VentaRequest`, `VentaResponse`, `DetalleVentaResponse`,
etc.) reflejando exactamente los DTO reales del backend — contrato verificado con
`curl` contra el backend real (dejado corriendo por el propietario, no se tocó
`docker compose down` en ningún momento), campo por campo, incluyendo el caso donde
`ProductoResponseDTO` (embebido en el detalle de venta) usa `categoria`/`proveedor`
como `String` con el nombre, a diferencia de `ProductoDetalleDTO` que sí los anida como
objeto.

`VentaService.crear()` → `POST /ventas`, con spec. `CarritoService` ganó `carritoId`
(UUID perezoso vía `crypto.randomUUID()`, se renueva en `vaciar()`), con specs nuevos.

`CobroComponent` (`features/pos/cobro/`), montado debajo de `<app-carrito>` dentro del
mismo `<aside>`: selector de método de pago con los 4 valores reales del backend
(`EFECTIVO`, `TARJETA_CREDITO`, `TARJETA_DEBITO`, `TRANSFERENCIA` — la nota de "método
único" de `COBRO_POS.md` estaba desactualizada), monto recibido + cambio estimado en
vivo si es efectivo (contra el subtotal estimado del carrito, explícitamente
etiquetado como estimado), botón "Cobrar" deshabilitado si el carrito está vacío o ya
hay un envío en curso (previene duplicados). Al confirmar, arma el `VentaRequest` desde
`CarritoService` y muestra un recibo con **solo datos reales devueltos por el
servidor**: precio de lista, tipo/porcentaje/monto de descuento y subtotal por línea,
folio, subtotal/descuento/total definitivos, y cambio real recalculado contra el total
definitivo (nunca contra el estimado). Errores: distingue conexión (status 0),
401/403 sin cuerpo JSON (rechazo del filtro de seguridad antes del controlador — se
verificó con un `GERENTE` real intentando vender, bloqueado por
`SecurityConfig.hasAnyRole` a nivel de filtro, sin pasar por `GlobalExceptionHandler`)
de los que sí traen `mensaje` (`GlobalExceptionHandler`: stock, validación, rol vía
`@PreAuthorize`). El carrito no se vacía en error, para que el cajero pueda corregir.

Verificado con `ng build` (limpio; `pos-layout` crece de 14.95 kB a 22.81 kB),
`ng test` (5 tests nuevos — 1 `VentaService`, 2 `carritoId` en `CarritoService`, sin
fallas nuevas) y **`curl` contra el backend real** (dejado arriba por el propietario):
payload idéntico al que arma `cobrar()` confirmado campo por campo contra el
`VentaResponse` real; stock insuficiente, producto inexistente, sin token y rol
`GERENTE` bloqueado por `SecurityConfig` verificados con sus mensajes/códigos reales.

**Nota:** durante esta verificación se creó un usuario `gerente@pos.com` y una venta
de prueba en la base de datos que el propietario dejó corriendo (no es una base
efímera de `docker compose down -v`, así que este dato de prueba persiste). Queda a tu
criterio limpiarlo o conservarlo.

#### 4b — Botón de descuento manual y modal de reautenticación (completada, 2 de septiembre de 2026)

Implementado `autorizacion-descuento.model.ts` y `AutorizacionDescuentoService` (con
spec) → `POST /autorizaciones-descuento`, según el contrato ya documentado en
`AUTORIZACION_DESCUENTO_MANUAL.md`.

`LineaCarrito` gana `autorizacionManual: AutorizacionManualLinea | null`.
`CarritoService` gana `otorgarDescuentoManual()`/`quitarDescuentoManual()`; toda línea
nueva empieza sin autorización, y `establecerCantidad()` (usado también por
`incrementar`/`decrementar`/`agregarProducto` sobre una línea existente) la descarta
siempre que cambia la cantidad — cumple "cambiar cantidad descarta la autorización
anterior" sin lógica adicional, con 4 specs nuevos cubriendo alta/baja y la
invalidación por cambio de cantidad o por re-agregar el mismo producto.

`ModalAutorizacionDescuentoComponent` (`features/pos/modal-autorizacion-descuento/`):
formulario (usuario, contraseña, porcentaje 0–30, motivo) → al autorizar, fase de
revisión mostrando el descuento, precio final estimado, y la comparación contra la
mejor promoción automática disponible (todo lo devuelve el propio endpoint); botones
"Aplicar a la venta" o "Descartar" (descartar no cancela nada en el backend — no existe
ese endpoint — simplemente la referencia no se usa y expira sola a los 2 minutos). La
contraseña se limpia del estado del componente inmediatamente después de armar la
solicitud (antes de esperar la respuesta), nunca se autocompleta
(`autocomplete="off"`), y se vuelve a limpiar al cancelar/cerrar en cualquier fase.

`CarritoComponent`: botón "Cambiar descuento" por línea (oculto si la línea ya tiene
una autorización aplicada, reemplazado por un badge con el porcentaje y un enlace
"Quitar"); el modal se monta condicionalmente para la línea con `lineaModalAbierta()`.

`CobroComponent`: cada línea con `autorizacionManual` incluye su `referencia` como
`autorizacionDescuento` en el `DetalleVentaRequest` al confirmar.

Verificado con `ng build` (limpio; `pos-layout` crece de 22.81 kB a 33.68 kB),
`ng test` (5 tests nuevos — 4 `CarritoService`, 1 `AutorizacionDescuentoService`; sin
fallas nuevas) y **flujo real completo por `curl`** contra el backend que el
propietario dejó corriendo: solicitar autorización (payload idéntico al del modal) →
confirmar venta con la referencia (payload idéntico al de `CobroComponent`) →
`tipoDescuento: MANUAL` con el porcentaje/monto/autorizador correctos en la respuesta
real → reutilizar la misma referencia rechazada con mensaje → contraseña incorrecta
del autorizador rechazada con `401` y mensaje. Todos los mensajes de error verificados
coinciden exactamente con lo que `mensajeDeError()` del modal espera extraer.

Con esto, **la Sub-tarea 4 completa (y el alcance original de la Tarea 7) queda
implementada.**

## Pendientes no bloqueantes (documentados para implementar después)

- **Perfil del usuario autenticado.** El JWT solo lleva `username`; `AuthResponse` no
  expone nombre, rol ni tienda. Documentado en
  `docs/requerimientos/autenticacion/LOGIN_JWT.md`. No bloquea ninguna sub-tarea
  conocida (para el ticket de venta, `VentaResponseDTO` ya trae el `usuario` completo
  tras confirmar el cobro); relevante solo si se quiere mostrar "Cajero: <nombre>" en
  la barra superior del POS antes de vender, o condicionar UI al rol del usuario en
  sesión sin una llamada adicional.
- **Cambio obligatorio de contraseña ignorado por el frontend**
  (`CAMBIO_PASSWORD_OBLIGATORIO.md`, ya documentado como pendiente ahí): `login`
  navega directo a `/pos` sin mirar `requiereCambioPassword`. No es parte del alcance
  de ninguna sub-tarea de este documento; se deja para cuando se aborde ese
  requerimiento explícitamente.
- **Renovación automática de token** (`REFRESH_TOKEN.md`, ya documentado como
  pendiente ahí): el frontend no solicita un `access_token` nuevo automáticamente al
  expirar. Podría volverse molesto durante una venta larga en el POS, pero no bloquea
  construir las sub-tareas 1–4.
- **Búsqueda de clientes en servidor**: `GET /api/v1/clientes` devuelve la lista
  completa sin paginar ni filtrar. El selector de cliente del carrito (Sub-tarea 3) usa
  un `<select>` nativo sobre esa lista completa, razonable mientras el número de
  clientes sea pequeño. Si la base de clientes crece, hará falta un endpoint de
  búsqueda/paginación en el backend antes de que un `<select>` simple deje de ser
  utilizable.
- **Puntero desactualizado en `frontend/CLAUDE.md`**: referencia
  `docs/planeacion_tareas.md (raíz del repo)` como fuente de tareas secuenciales; ese
  archivo vive ahora en `docs/legacy/planeacion_tareas.md` y ya no es vigente. Debe
  actualizarse para apuntar a `docs/requerimientos/pos/` y a este documento.

## Orden y reglas para retomar

1. Ejecutar las sub-tareas 1 a 4 en ese orden, salvo que un descubrimiento obligue a
   replantear dependencias.
2. Antes de cada sub-tarea, inspeccionar el código real y presentar análisis y plan al
   propietario.
3. Implementar una sola sub-tarea por vez.
4. Verificarla (`ng build`, y `ng test` si aplica) y detenerse para revisión y commit
   manual del propietario.
5. Cualquier gap de contrato de API descubierto durante el frontend se documenta (no
   se modifica `backend/` desde estas sub-tareas sin acuerdo explícito).

## Requerimientos fuente

- `docs/requerimientos/pos/INTERFAZ_POS.md`
- `docs/requerimientos/pos/BUSQUEDA_PRODUCTOS_POS.md`
- `docs/requerimientos/pos/CARRITO_VENTA.md`
- `docs/requerimientos/pos/COBRO_POS.md`
- `docs/requerimientos/ventas/AUTORIZACION_DESCUENTO_MANUAL.md`
- `docs/requerimientos/autenticacion/LOGIN_JWT.md`
- `docs/trabajo-completado/TAREAS_VENTAS.md` (Tarea 7, origen de este documento)
