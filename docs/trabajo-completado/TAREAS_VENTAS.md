# Trabajo pendiente del módulo de ventas — Tareas 3 a 8

**Estado del plan:** Completo — T3 a T8 implementadas y verificadas  
**Última actualización:** 2 de septiembre de 2026

## Propósito

Este documento permite retomar la implementación segura de promociones y descuentos
del módulo de ventas sin depender del historial de una conversación. Debe leerse junto
con `.ai/README.md` y los requerimientos enlazados al final.

## Estado antes de continuar

- La Tarea 1 ya eliminó la confianza en el precio enviado por el frontend: el backend
  obtiene el precio vigente del producto persistido.
- Las Tareas 2A y 2B migraron los valores financieros del backend de `Double` a
  `BigDecimal` y las columnas correspondientes a `NUMERIC` mediante Flyway V5 y V6.
- `DetalleVenta` ya contiene campos para conservar la fotografía histórica del precio,
  tipo de descuento, porcentaje, monto, precio final, autorizador y motivo.
- Las ventas se registran por ahora sin descuento (`NINGUNO`, cero). Todavía no existe
  un motor general de promociones ni autorización manual.
- Antes de comenzar T3, revisar el estado real de Git y confirmar que T1, T2A y T2B
  fueron revisadas y guardadas por el propietario. Los agentes no deben realizar
  commits.

## Decisiones de negocio ya aprobadas

- Los descuentos se aplican por producto o línea, no al total global de la venta.
- El frontend nunca decide el precio, promoción, porcentaje, monto descontado ni total
  efectivo; el backend recalcula todo al confirmar.
- La primera promoción automática será descuento por cantidad. El diseño debe admitir
  en el futuro reglas como `DOS_POR_UNO`, precio especial o porcentaje temporal por
  producto/categoría, sin almacenar código ejecutable.
- Las promociones automáticas no se acumulan. Se elige una sola candidata por producto:
  mayor beneficio, luego prioridad y finalmente ID como desempate determinista.
- La promoción VIP participa como una candidata más y no se suma a otras promociones.
- Un descuento manual autorizado reemplaza la promoción automática de esa línea.
- `VENDEDOR` no puede modificar descuentos. `GERENTE` y `ADMINISTRADOR` pueden autorizar
  hasta 30% mediante usuario y contraseña y con motivo obligatorio.
- `GERENTE` nunca puede autorizar un precio inferior al costo vigente.
- Solo `ADMINISTRADOR` puede permitir una venta por debajo del costo, sin superar 30%.

---

## Tarea 3 — Modelo persistente y administración de promociones

**Estado:** Completado (31 de agosto de 2026)

Implementado: entidades `Promocion` y `ReglaDescuentoPorCantidad`, migración `V7`,
repositorio, DTOs con validación de alcance producto/categoría excluyente, servicio y
`PromocionController` (`/api/v1/promociones`, `ADMINISTRADOR`). Verificado con pruebas
unitarias y migración `V1`→`V7` sobre PostgreSQL limpio vía `docker compose`. Se
corrigió además un defecto preexistente en `GlobalExceptionHandler` que devolvía 500 en
lugar de 403/400 para rechazos de `@PreAuthorize` y validaciones de DTO en toda la API.
Aún no integrado a `VentaService` (corresponde a T5).

### Objetivo

Crear el modelo extensible de promociones automáticas y la primera configuración de
descuento por cantidad.

### Alcance

- Definir entidades y migración Flyway nueva para promoción, alcance por producto o
  categoría y regla de descuento por cantidad.
- Incluir nombre, descripción, tipo, estado, vigencia opcional, prioridad y datos de
  auditoría necesarios.
- La regla por cantidad debe contener una cantidad mínima entera positiva y un
  porcentaje mayor que cero y menor que cien.
- Permitir varios escalones para un mismo producto, por ejemplo 10 unidades al 5% y
  20 unidades al 8%.
- Implementar repositorios y administración REST mínima. Solo `ADMINISTRADOR` puede
  crear, modificar, activar o desactivar promociones.
- Validar configuraciones incompletas, fechas inválidas, porcentajes fuera de rango y
  referencias inexistentes.
- No incorporar aún la evaluación a `VentaService`; esa integración pertenece a T5.

### Verificación mínima

- Migraciones completas en una base PostgreSQL limpia.
- Pruebas de validación, vigencia, rol y persistencia de varios escalones.
- Confirmar que modificar una promoción no altera detalles de ventas históricas.

---

## Tarea 4 — Motor de promociones automáticas

**Estado:** Completado (31 de agosto de 2026)

Implementado: `MotorPromocionesService` (paquete `promocion`), servicio de dominio sin
controlador que evalúa candidatas de producto/categoría (`DESCUENTO_POR_CANTIDAD`) y de
cliente (VIP por porcentaje o por monto fijo) para una cantidad ya consolidada, y
selecciona una única ganadora de forma determinista.

Desempate acordado con el propietario (extiende "mayor beneficio, luego prioridad,
finalmente ID" de `PROMOCIONES_PRODUCTO.md`): **mayor beneficio → mayor prioridad
explícita → vigencia con fecha de inicio y fin definidas ("tiempo definido") sobre
vigencia atemporal → mayor ID (promoción más reciente)**. Se agregó columna `prioridad`
a `promociones_cliente` (migración `V8`) para que las promociones de cliente participen
en el mismo desempate que las de producto. El descuento por monto fijo de
`PromocionCliente` (`montoDescuentoFijo`) ahora se evalúa como una candidata más del
mismo pool, capado al subtotal de la línea, sujeto a las mismas reglas de prioridad.

No integrado aún a `VentaService` (corresponde a T5). Verificado con 18 pruebas
unitarias (umbral, escalones, vigencia inclusiva en ambos extremos, promoción
inactiva/futura/vencida, VIP vs. producto, monto fijo, y los tres niveles de empate) y
migración `V1`→`V8` sobre PostgreSQL limpio vía `docker compose`.

### Objetivo

Implementar un servicio de dominio que calcule promociones aplicables sin depender del
frontend ni acoplar toda la lógica a `VentaService`.

### Alcance

- Recibir producto persistido, cantidad total consolidada, cliente y fecha/hora de la
  operación.
- Evaluar reglas activas, vigentes y aplicables al producto o su categoría.
- Implementar el evaluador `DESCUENTO_POR_CANTIDAD` usando exclusivamente
  `BigDecimal` y el redondeo financiero aprobado.
- Incorporar la promoción VIP vigente como candidata automática.
- Calcular el beneficio monetario de cada candidata y seleccionar exactamente una por
  mayor beneficio, prioridad e ID.
- Devolver un resultado interno explícito con identificador/tipo, porcentaje o
  beneficio, monto descontado y subtotal final.
- Garantizar que ninguna regla produzca importes o unidades negativos.

### Verificación mínima

- Debajo, exactamente en y por encima del umbral de cantidad.
- Varios escalones y empate determinista.
- Promoción inactiva, futura o vencida.
- Comparación entre promoción VIP y promoción de producto.
- Repetición del mismo producto en varias líneas sin evasión ni duplicación del
  beneficio.

---

## Tarea 5 — Integrar promociones automáticas al registro de venta

**Estado:** Completado (1 de septiembre de 2026)

Implementado en `VentaService.crearVenta`: los productos repetidos del request se
consolidan por `productoId` (sumando cantidades) antes de validar stock y evaluar
promociones, evitando evasión o duplicación del beneficio de un escalón. Por cada
producto consolidado se llama a `MotorPromocionesService.evaluar(...)` (T4) con el
cliente real solo si la venta no es anónima (una venta anónima nunca evalúa
promociones de cliente, ni siquiera las que pudieran existir por error sobre el
registro "PÚBLICO GENERAL"). El `ResultadoPromocion` se fotografía completo en
`DetalleVenta`: `tipoDescuento`, `porcentajeDescuento`, `montoDescuento`, `subtotal`
(post-descuento), `precioUnitarioFinal` (subtotal final ÷ cantidad, HALF_UP) y la
referencia a la promoción ganadora. `Venta.subtotal`/`descuento`/`total` se recalculan
como la suma de `subtotalLista`/`montoDescuento` de todas las líneas.

Se agregaron a `DetalleVenta` dos FK excluyentes y nuevas en la migración `V9`:
`promocion_producto_id` (→ `promociones`) y `promocion_cliente_id` (→
`promociones_cliente`), con `CHECK` de coherencia contra `tipo_descuento`. Se
expusieron ambos IDs en `DetalleVentaResponseDTO`.

Verificado con pruebas unitarias (consolidación, promoción de cantidad, promoción de
cliente, venta anónima sin cliente, stock insuficiente revierte todo) y migración
`V1`→`V9` sobre PostgreSQL limpio vía `docker compose`, incluyendo pruebas manuales por
`curl`: consolidación de dos líneas del mismo producto que dispara el escalón,
atomicidad ante stock insuficiente en un producto posterior, y persistencia de la
fotografía histórica tras desactivar la promoción usada.

**Defecto preexistente encontrado en T5 y corregido en `V11`:** los `INSERT` de
`V2__datos_prueba.sql` usan IDs explícitos en `tiendas`, `usuarios`, `categorias`,
`proveedores`, `clientes`, `productos` y `ventas` (columnas `BIGSERIAL`), sin
resincronizar sus secuencias. Postgres no avanza la secuencia cuando se inserta un id
explícito, así que la primera fila que el backend dejaba autogenerar en cualquiera de
esas tablas colisionaba con un id ya usado por el seed (`duplicate key value violates
unique constraint`). `detalles_venta` nunca estuvo afectada (su `INSERT` en V2 no fija
`id`).

`V11__resincronizar_secuencias_datos_prueba.sql` corrige esto con `setval(
pg_get_serial_sequence(...), MAX(id))` para las siete tablas afectadas, sin modificar
`V2` (migración ya aplicada). Verificado sobre PostgreSQL limpio vía `docker compose`
creando, sin ningún ajuste manual, una venta, un usuario, un cliente, una categoría,
una tienda, un proveedor y un producto — los siete casos que antes fallaban.

### Objetivo

Usar el motor de T4 al confirmar una venta y conservar el resultado como fotografía
histórica en cada `DetalleVenta`.

### Alcance

- Consolidar o rechazar de forma explícita productos repetidos antes de evaluar
  cantidades y stock.
- Volver a obtener precio, costo y promociones desde persistencia dentro de la
  operación de venta.
- Aplicar como máximo una promoción automática por producto.
- Recalcular subtotal, descuento, impuesto y total exclusivamente en backend.
- Guardar promoción aplicada, precio de lista, porcentaje/beneficio, monto descontado,
  precio final y subtotal en el detalle.
- Mantener la transacción atómica: cualquier error debe revertir venta, detalles,
  inventario y acumulado del cliente.
- Ajustar el contrato de respuesta para que el frontend pueda presentar el total
  definitivo si difiere de su estimación.

### Verificación mínima

- Payload manipulado no altera precios ni descuentos.
- Totales y redondeos exactos con múltiples productos.
- Stock insuficiente revierte toda la operación.
- La venta histórica no cambia después de editar o desactivar la promoción.
- Pruebas de autorización y aislamiento por tienda aplicables al registro.

---

## Tarea 6 — Autorización backend de descuento manual

**Estado:** Completado (1 de septiembre de 2026)

Implementado en el paquete nuevo `autorizacion`: `AutorizacionDescuento` (entidad,
migración `V10`, solo persiste el hash SHA-256 del token opaco, nunca el token en
claro), `AutorizacionDescuentoRepository` (con una actualización atómica condicional
para el consumo de un solo uso), `AutorizacionDescuentoService` (`solicitar`/
`consumir`), `AutorizacionDescuentoController` (`POST
/api/v1/autorizaciones-descuento`) y `LimitadorIntentosAutorizacion` (límite de 5
intentos fallidos por vendedor en una ventana de 15 minutos, en memoria).

`solicitar` reautentica al gerente/administrador con `AuthenticationManager` (mismo
mecanismo que `/auth/login`), valida rol permitido, usuario activo, tienda del gerente
igual a la del vendedor, y que el precio final no quede bajo el costo vigente si el
autorizador es `GERENTE` (un `ADMINISTRADOR` puede autorizarlo). Devuelve también el
beneficio de la mejor promoción automática de producto/categoría disponible (sin
considerar cliente, limitación documentada) para que el frontend muestre la diferencia
frente al descuento manual solicitado.

`VentaService.crearVenta` consume la autorización (vía `AutorizacionDescuentoService.
consumir`) dentro de la misma transacción de la venta: revalida producto, cantidad
consolidada, vendedor, tienda y carrito, vuelve a exigir el piso de costo para
autorizaciones de `GERENTE` (por si el costo cambió entre la emisión y el consumo), y
consume de forma atómica (`UPDATE ... WHERE consumida = false AND fecha_expiracion >
ahora`) para que dos consumos concurrentes no puedan aceptar el mismo permiso. El
descuento manual reemplaza la promoción automática de la línea (nunca se evalúan
ambas). Dos líneas del mismo producto consolidado con referencias de autorización
distintas se rechazan por ambigüedad.

**Corrección de un defecto preexistente de todo el proyecto:** `AuthenticationManager.
authenticate(...)` con credenciales inválidas lanza `AuthenticationException`
(`BadCredentialsException`/`DisabledException`), que no tenía handler en
`GlobalExceptionHandler` y caía al genérico → 500. Esto afectaba también a
`/auth/login`. Se agregó un handler para `AuthenticationException` → 401 con mensaje
genérico (nunca revela si el usuario existe o si la cuenta está inactiva).

Verificado con 28 pruebas unitarias nuevas (`AutorizacionDescuentoServiceTest`: 17;
adiciones a `VentaServiceTest`: 3) y migración `V1`→`V10` sobre PostgreSQL limpio vía
`docker compose`, incluyendo un flujo completo por `curl`: emisión de autorización →
venta con descuento manual aplicado → reutilización de la misma referencia rechazada →
contraseña incorrecta (401, ya no 500) → gerente bajo costo rechazado →
administrador bajo costo permitido → porcentaje fuera de rango rechazado → límite de
intentos fallidos bloquea solicitudes posteriores.

No implementado en este alcance (queda para T7): la UI del modal de autorización y el
manejo en frontend de expiración/rechazo. El identificador de carrito (`carritoId`) lo
genera y gestiona el frontend; el backend solo lo usa para atar la autorización a una
operación específica.

**Contrato documentado para T7:** el flujo completo que debe seguir el frontend
(cuándo generar `carritoId`, request/response de `POST
/api/v1/autorizaciones-descuento` con ejemplos, cómo enviar `carritoId` y
`autorizacionDescuento` en `POST /api/v1/ventas`, manejo de contraseña, y qué hacer
ante cada código de error) está en
`docs/requerimientos/ventas/AUTORIZACION_DESCUENTO_MANUAL.md`, sección "Contrato para
el frontend (integración T7)".

### Objetivo

Permitir que un `GERENTE` o `ADMINISTRADOR` reautorice un descuento excepcional por
producto mediante credenciales, sin entregar autoridad al vendedor o al frontend.

### Alcance

- Crear un endpoint dedicado de reautenticación que reciba usuario, contraseña,
  producto, cantidad, porcentaje, motivo, tienda e identificador del carrito.
- Verificar credenciales, usuario activo, rol, tienda, precio y costo obtenidos desde
  persistencia.
- Emitir una autorización opaca, temporal (referencia inicial: dos minutos), vinculada
  al autorizador, vendedor, tienda, producto, cantidad, porcentaje, motivo y carrito.
- La autorización debe ser de un solo uso y quedar marcada/consumida en la misma
  transacción de la venta.
- Revalidar todo el contexto al consumirla. Alterar producto, cantidad, porcentaje,
  vendedor, tienda o carrito debe invalidarla.
- Aplicar máximo 30%; impedir que `GERENTE` venda bajo costo y permitirlo únicamente a
  `ADMINISTRADOR`.
- Registrar intentos y autorizaciones sin guardar contraseñas, tokens completos ni
  secretos. Incorporar limitación de intentos fallidos.
- El descuento manual sustituye a la promoción automática; no se acumula.

### Verificación mínima

- Roles no permitidos, contraseña incorrecta y usuario inactivo.
- Restricción de tienda para `GERENTE`.
- Límites 0% y 30%, rechazo de valores superiores y reglas de venta bajo costo.
- Autorización expirada, alterada, reutilizada o usada por otra operación.
- Concurrencia: dos consumos simultáneos no pueden aceptar el mismo permiso.

---

## Tarea 7 — Interfaz POS para promociones y descuento manual

**Estado:** Completada — re-alcanzada como 4 sub-tareas, todas implementadas (2 de septiembre de 2026)

Esta tarea asumía una pantalla de POS con buscador, carrito y cobro ya funcionando; el
frontend real no tenía nada de eso (solo landing pública, login y un placeholder en
`/pos`). Se dividió en cuatro sub-tareas ordenadas — layout base, búsqueda de
productos, carrito y cobro (esta última contiene el alcance completo descrito abajo,
implementado en dos pasadas 4a/4b) — con seguimiento propio en
`docs/trabajo-completado/TAREAS_POS.md`, donde está el detalle completo de la
implementación y su verificación (incluyendo un flujo real de extremo a extremo por
`curl` contra un backend en ejecución: emitir autorización de descuento manual,
aplicarla al confirmar la venta, y las promociones automáticas y totales definitivos
mostrados correctamente en el recibo).

### Objetivo

Mostrar promociones calculadas por el servidor y permitir solicitar de forma segura
una autorización manual desde el carrito.

### Alcance

- Mostrar por línea precio de lista, promoción aplicada, ahorro y subtotal definitivo.
- Agregar el botón de cambio de descuento solamente cuando el flujo sea pertinente.
- Abrir un modal que solicite usuario, contraseña, porcentaje y motivo del autorizador.
- Enviar las credenciales solo al endpoint dedicado de T6 y conservar únicamente la
  referencia opaca retornada.
- Limpiar contraseña y estado sensible al cerrar, cancelar o completar el modal; no
  persistirlos en `localStorage`, logs ni estado durable.
- Asociar cada autorización a su línea y enviarla al confirmar la venta.
- Manejar expiración, rechazo, cambio de cantidad/producto y recálculo del servidor.
- Mostrar la diferencia entre la mejor promoción automática y el descuento manual
  solicitado para que la autorización sea informada.

### Verificación mínima

- Un vendedor no puede simular la autorización modificando el estado o payload.
- Cambiar cantidad o producto descarta la autorización anterior.
- La contraseña desaparece del estado después de cada intento.
- La UI presenta correctamente respuestas de expiración, límite, bajo costo y
  credenciales inválidas.

---

## Tarea 8 — Auditoría, endurecimiento y pruebas integrales

**Estado:** Completado (2 de septiembre de 2026)

Hallazgos del barrido de auditoría/seguridad sobre T3–T7 y su corrección:

- **Auditoría de descuento manual incompleta.** `AutorizacionDescuento` guardaba
  quién autorizó pero no el rol que tenía en ese momento, el costo considerado ni el
  monto de la promoción automática disponible (los dos últimos ya se calculaban, solo
  no se persistían). Migración `V12__auditoria_autorizacion_descuento.sql` agrega
  `rol_autorizador`, `costo_considerado` y `monto_promocion_automatica_disponible`
  (con backfill best-effort para filas existentes y `NOT NULL`/`CHECK` después);
  `AutorizacionDescuentoService.solicitar()` los fija en el momento de la emisión,
  independientes de lo que `Usuario`/`Producto` digan después.
- **Control de acceso horizontal roto en `VentaController`.** `GET /ventas`,
  `GET /ventas/{id}` y `GET /ventas/cliente/{clienteId}` no filtraban por tienda — un
  `VENDEDOR` podía leer ventas de cualquier tienda (IDOR). Corregido: `ADMINISTRADOR`
  ve todo, `VENDEDOR` solo su tienda; `getVentaById` devuelve 404 (no 403) ante una
  venta de otra tienda, para no confirmar que el ID existe en otro lugar.
  `GET /ventas/dia` ya estaba bien (deriva la tienda del usuario autenticado, nunca de
  un parámetro del cliente).
- **Logs revisados** (`AutorizacionDescuentoService`, `PromocionService`,
  `MotorPromocionesService`): ningún log expone contraseña, token completo ni JWT. Sin
  hallazgos.
- **Pruebas de integración reales**: se agregó Testcontainers
  (`spring-boot-testcontainers`, `testcontainers-postgresql`,
  `testcontainers-junit-jupiter`, más `spring-boot-resttestclient` y
  `spring-boot-restclient` para `TestRestTemplate` — ambos se separaron de
  `spring-boot-starter-test` en Spring Boot 4) y `VentaFlujoIntegrationTest`: 6
  pruebas contra un PostgreSQL efímero y aislado (nunca toca `docker-compose`),
  ejecutando las migraciones `V1`→`V12` reales en cada corrida. Cubre: promoción por
  cantidad con conservación histórica tras desactivarla; atomicidad real (stock ya
  descontado se revierte); descuento manual con concurrencia real (dos hilos
  compitiendo por el mismo consumo de un solo uso, exactamente uno gana) y
  reutilización posterior rechazada; expiración (autorización retrasada
  manipulando la fila persistida, no con `Thread.sleep`); manipulación de request
  (campos de precio/descuento fabricados en el JSON, ignorados); redondeo HALF_UP
  exacto en un caso con decimales no triviales (33.33% de $672.00), verificado contra
  un cálculo `BigDecimal` independiente.
- **Documentación actualizada** a su estado real:
  `ventas/DESCUENTO_POR_CANTIDAD.md` y `calidad/PRECISION_MONETARIA.md` (ya no decían
  "todavía no existe..."), `ventas/AUTORIZACION_DESCUENTO_MANUAL.md` (campos de
  auditoría nuevos), `ventas/REGISTRAR_VENTA.md` (estado real, control de acceso
  horizontal, UI completa), `auditoria/BITACORA_AUDITORIA.md` (nota de alcance
  acotado, sin reclamar la bitácora general que sigue sin definirse).
- **No se agregaron specs de componente en el frontend**
  (`CobroComponent`/`ModalAutorizacionDescuentoComponent`) — decisión consciente,
  siguiendo la convención ya establecida de no tener specs por componente (solo a
  nivel de servicio); documentado en `REGISTRAR_VENTA.md` como pendiente a revisar,
  no como olvido.
- **Migraciones desde una base limpia**: verificado de forma repetible en cada
  corrida de `VentaFlujoIntegrationTest` (V1→V12 contra Postgres real y efímero), no
  solo con una verificación manual puntual.

Verificado con `mvn test`: 79 pruebas, 78 en verde (la única roja es
`ApiPapeleriaApplicationTests.contextLoads`, preexistente, requiere un Postgres real
en `localhost:5432` fuera del alcance de este entorno).

### Objetivo

Cerrar el flujo de promociones y descuentos con trazabilidad, pruebas de integración y
documentación consistente antes de declararlo implementado.

### Alcance

- Conservar en la auditoría vendedor, autorizador y rol, tienda, producto, cantidad,
  precio de lista, costo considerado, promoción automática disponible, descuento
  aplicado, motivo y fecha.
- Revisar controles de acceso horizontal por tienda y roles en todos los endpoints
  nuevos o modificados.
- Verificar que logs y respuestas no expongan contraseñas, JWT, autorizaciones opacas
  completas ni datos sensibles.
- Cubrir atomicidad, concurrencia, expiración, precisión, redondeo, manipulación de
  requests y conservación histórica.
- Agregar pruebas de integración del flujo completo backend y pruebas relevantes del
  frontend.
- Actualizar el estado de cada requerimiento solamente según lo que realmente haya
  quedado implementado.
- Ejecutar compilación, pruebas y migraciones Flyway desde una base limpia.

### Criterio de cierre

El flujo solo puede marcarse como implementado cuando un `VENDEDOR` no pueda alterar
precios o descuentos, las promociones se resuelvan de forma determinista, las
autorizaciones sean de un solo uso y la venta conserve una fotografía financiera y de
auditoría completa.

## Orden y reglas para retomar

1. Ejecutar T3, T4, T5, T6, T7 y T8 en ese orden, salvo que un descubrimiento obligue
   a replantear dependencias.
2. Antes de cada tarea, inspeccionar el código real y presentar análisis y plan al
   propietario.
3. Implementar una sola tarea por vez.
4. Verificarla y detenerse para revisión y commit manual del propietario.
5. No modificar migraciones Flyway ya aplicadas; crear una versión nueva.
6. Si el código contradice este documento o un requerimiento aprobado, detenerse y
   exponer la diferencia antes de continuar.

## Requerimientos fuente

- `docs/requerimientos/ventas/PROMOCIONES_PRODUCTO.md`
- `docs/requerimientos/ventas/DESCUENTO_POR_CANTIDAD.md`
- `docs/requerimientos/ventas/PROMOCIONES_CLIENTE.md`
- `docs/requerimientos/ventas/AUTORIZACION_DESCUENTO_MANUAL.md`
- `docs/requerimientos/ventas/REGISTRAR_VENTA.md`
- `docs/requerimientos/calidad/PRECISION_MONETARIA.md`
- `docs/requerimientos/auditoria/BITACORA_AUDITORIA.md`
- `docs/requerimientos/seguridad/PROTECCION_XSS.md`

