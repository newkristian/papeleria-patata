# Plan de trabajo — Carga de productos, inventario y proveedores

**Estado del plan:** En desarrollo — Tareas 1, 2, 3 y 4 completadas
**Última actualización:** 2 de septiembre de 2026

## Propósito

Este documento permite implementar y verificar la administración de productos,
proveedores, inventario y fotografías sin depender del historial de una conversación.
Debe leerse junto con `.ai/README.md` y con los requerimientos funcionales enlazados al
final.

La implementación debe realizarse una tarea a la vez. Después de cada tarea se debe
presentar el resultado al propietario, esperar su revisión y permitir que realice el
commit antes de continuar. Los agentes no deben realizar commits.

## Estado verificado antes de comenzar

- El backend ya permite crear, consultar y actualizar productos, pero no existe una
  pantalla administrativa en Angular.
- `Producto`, los DTO y Flyway contienen `cantidadDesconocida`; la creación inicia
  `stockActual` en cero y un ajuste absoluto desactiva la bandera.
- Las ventas y salidas manuales omiten la validación de existencias desconocidas, pero
  actualmente sí descuentan `stockActual` y pueden generar cantidades negativas.
- La relación entre producto y proveedor es obligatoria en DTO, JPA y PostgreSQL.
- El backend contiene un CRUD básico de proveedores, sin autorización explícita en las
  escrituras ni tratamiento seguro de relaciones al eliminar.
- Existen endpoints para cargar, listar, descargar, eliminar y seleccionar la foto
  principal. El procesamiento y la generación de miniatura son síncronos y las
  validaciones de archivo son insuficientes.
- El frontend solo consume el catálogo desde el POS. No existen rutas ni componentes
  administrativos para productos, proveedores o inventario.
- El contrato de autenticación no expone rol ni tienda al frontend.
- El backend compila en el estado inicial. La suite no termina correctamente en el
  entorno inspeccionado porque Testcontainers no puede acceder a Docker y Mockito no
  puede adjuntar Byte Buddy.
- En frontend pasan 25 de 26 pruebas; `app.spec.ts` contiene una expectativa obsoleta.
  `ng build` falla en el entorno inspeccionado por un deadlock interno de esbuild.
- Antes de implementar se debe volver a revisar `git status`. Ya existen archivos no
  rastreados que no pertenecen a este plan y deben preservarse.

## Decisiones de negocio aprobadas

### Proveedor pendiente

- La relación `Producto -> Proveedor` continuará siendo obligatoria. No se hará
  nullable `productos.proveedor_id` ni se alterará la cardinalidad existente.
- Se creará un proveedor reservado con nombre exacto `PENDIENTE`.
- Cuando el usuario no seleccione proveedor al crear un producto, el backend asignará
  `PENDIENTE`. El frontend podrá presentarlo como “Proveedor pendiente”.
- La asignación se resolverá siempre en backend; el cliente no enviará ni dependerá
  del ID concreto del proveedor reservado.
- `PENDIENTE` no podrá renombrarse, eliminarse ni utilizarse para pagos o comisiones.
- El sistema deberá garantizar que exista exactamente un proveedor reservado y
  resolverlo por una clave protegida, sin asumir un ID generado específico.

### Eliminación de proveedores

- La eliminación de proveedores será lógica, no física, para preservar historial,
  pagos y referencias.
- Antes de desactivar un proveedor, todos sus productos se reasignarán a `PENDIENTE`
  dentro de la misma transacción.
- El proveedor reservado no podrá desactivarse.
- Los listados de selección mostrarán proveedores activos y excluirán `PENDIENTE`
  salvo cuando sea necesario representar el valor actual de un producto.
- Los reportes históricos conservarán las fotografías monetarias ya persistidas; los
  reportes que consulten la relación actual deberán documentar que una reasignación
  cambia la agrupación futura por proveedor.

### Inventario con cantidad desconocida

- Mientras `cantidadDesconocida = true`, una venta puede incluir el producto sin
  validar existencias y no debe modificar `stockActual`.
- Las entradas y salidas relativas no se usarán para construir una cantidad parcial
  mientras el producto siga marcado como desconocido.
- El conteo se completa por producto mediante un ajuste absoluto no negativo. Esta
  operación fija `stockActual` y cambia `cantidadDesconocida` a `false` de forma
  atómica.
- Después del conteo, ventas, entradas, salidas y ajustes seguirán el control normal de
  existencias y nunca podrán producir stock negativo.
- Los productos con cantidad desconocida no se considerarán con stock bajo.

### Ciclo de vida de productos

- Los productos no se borrarán físicamente.
- La eliminación funcional se implementará como desactivación (`activo = false`).
- Un producto inactivo conservará ventas, movimientos, promociones y fotografías.
- Un producto inactivo no podrá venderse ni recibir movimientos de inventario, pero
  podrá consultarse y reactivarse por un rol autorizado.

### Fotografías

- Cada subida admitirá como máximo **4 MB** tanto en configuración multipart como en
  validación de aplicación.
- Solo se aceptarán imágenes raster JPEG y PNG. No se admitirán SVG, GIF animado,
  WebP ni formatos cuyo decodificador no esté soportado explícitamente.
- El backend verificará firma/contenido real y capacidad de decodificación; nunca
  confiará únicamente en extensión o `Content-Type` proporcionados por el cliente.
- Se rechazará una imagen con dimensiones o cantidad de píxeles de origen excesivas
  antes de decodificarla por completo, para reducir riesgo de bombas de
  descompresión y agotamiento de memoria.
- La imagen normalizada mantendrá proporción, se reorientará cuando corresponda, se
  recodificará para eliminar metadatos y tendrá un máximo de **512 × 512 px**.
- Se generará una miniatura cuadrada de **80 × 80 px** usando recorte centrado y sin
  deformación.
- La generación de la imagen normalizada y la miniatura será asíncrona mediante un
  ejecutor acotado. No se crearán hilos directamente por solicitud.
- El archivo recibido se almacenará primero en un área temporal no pública. La base de
  datos registrará un estado explícito de procesamiento: `PENDIENTE`, `PROCESANDO`,
  `LISTA` o `ERROR`.
- El endpoint de carga devolverá `202 Accepted` con el identificador y estado. El
  frontend consultará el estado hasta obtener `LISTA` o `ERROR`, con espera y número
  de intentos acotados.
- Una imagen solo podrá establecerse como principal cuando esté `LISTA`. Mientras se
  procesa, la UI mostrará un placeholder.
- Ante fallo se eliminarán archivos parciales, se conservará un estado de error
  seguro y se permitirá reintentar o eliminar el registro.
- Las rutas y nombres físicos serán generados por el servidor. Los nombres originales
  se conservarán solo como metadato normalizado y nunca se usarán para resolver rutas.

## Matriz de autorización aprobada

| Operación | ADMINISTRADOR | GERENTE | INVENTARISTA | VENDEDOR |
|---|---:|---:|---:|---:|
| Consultar productos activos para POS | Sí | Sí | Sí | Sí |
| Consultar catálogo administrativo e inactivos | Sí | Sí | Sí | No |
| Crear y editar productos | Sí | Sí | Sí | No |
| Desactivar o reactivar productos | Sí | Sí | No | No |
| Consultar proveedores activos | Sí | Sí | Sí | No |
| Crear y editar proveedores | Sí | Sí | No | No |
| Desactivar proveedores | Sí | No | No | No |
| Registrar entradas, salidas y ajustes | Sí | Sí | Sí | No |
| Consultar costos e historial completo | Sí | Sí | Sí | No |
| Cargar, ordenar, marcar principal o eliminar fotos | Sí | Sí | Sí | No |
| Descargar imágenes listas usadas por el catálogo/POS | Sí | Sí | Sí | Sí |

Toda autorización real se aplicará en backend mediante `@PreAuthorize` y validaciones
de servicio cuando exista una regla de negocio adicional. Los guards y botones del
frontend solo reflejarán los permisos y no serán una frontera de seguridad.

## Mapa de relaciones de producto verificado

| Relación persistente | Nulabilidad | Momento | Impacto en el alta |
|---|---|---|---|
| `productos.categoria_id -> categorias.id` | `NOT NULL` | Alta/edición | Bloqueante: categoría existente |
| `productos.proveedor_id -> proveedores.id` | `NOT NULL` | Alta/edición | Bloqueante: proveedor real o `PENDIENTE` |
| `producto_fotos.producto_id -> productos.id` | `NOT NULL`, `ON DELETE CASCADE` | Posterior | Opcional; el producto puede nacer sin foto |
| `inventario_movimientos.producto_id -> productos.id` | `NOT NULL` | Posterior | No bloquea alta con cantidad desconocida |
| `detalles_venta.producto_id -> productos.id` | `NOT NULL` | Venta | No bloquea el alta; obliga a borrado lógico |
| `promociones.producto_id -> productos.id` | Nullable y excluyente con categoría | Posterior | Opcional; obliga a conservar referencias |
| `autorizaciones_descuento.producto_id -> productos.id` | `NOT NULL` | Venta autorizada | Opcional para el catálogo; conserva auditoría |

Dependencias funcionales adicionales del flujo:

- El usuario debe autenticarse como `ADMINISTRADOR` o `INVENTARISTA` y el frontend
  debe conocer su rol para habilitar el área administrativa.
- `costoCompra`, porcentaje de ganancia, unidad de medida y stock mínimo son campos de
  negocio obligatorios o con valores por defecto, pero no relaciones de tablas.
- La fotografía y el primer conteo ocurren después de persistir el producto; no deben
  hacer atómica una única solicitud grande de alta.
- El catálogo definitivo es global y el inventario futuro será por tienda, con
  transferencias. Dentro de este plan se conserva temporalmente el stock global
  compartido del modelo vigente; la evolución está documentada en
  `docs/requerimientos/inventario/STOCK_POR_TIENDA.md`.

---

## Tarea 1 — Actualizar requerimientos funcionales

**Estado:** Completada (2 de septiembre de 2026)

Se actualizaron los seis requerimientos funcionales previstos con las decisiones
aprobadas, su matriz de autorización, validaciones, pendientes y criterios de
aceptación. Se documentaron además dos dependencias no bloqueantes: la semántica de
los reportes históricos después de reasignar productos a `PENDIENTE` y la recuperación
de trabajos de imagen interrumpidos por un reinicio del backend. Ninguna de las dos
impide comenzar el backend del mantenimiento.

Revisión adicional del 2 de septiembre de 2026: se inspeccionaron todas las claves
foráneas y asociaciones de producto. Se incorporó `Categoria` como dependencia
bloqueante del flujo de alta y se añadió su mantenimiento frontend al plan. También se
documentaron como decisiones no bloqueantes el ciclo de vida de categorías y si el
catálogo/inventario global vigente debe evolucionar a inventario por tienda.

Decisión posterior: el catálogo será global y el stock será por tienda, incluyendo
transferencias. La implementación quedó explícitamente diferida; este plan mantiene
el stock global compartido como supuesto transitorio y registra la evolución en
`docs/requerimientos/inventario/STOCK_POR_TIENDA.md`.

### Objetivo

Sincronizar la fuente funcional vigente con las decisiones aprobadas antes de cambiar
código.

### Alcance

- Actualizar `CRUD_PRODUCTOS.md`, `CRUD_PROVEEDORES.md`,
  `CANTIDAD_DESCONOCIDA.md`, `MOVIMIENTO_INVENTARIO.md`,
  `AJUSTE_STOCK_MINIMO.md` y `GESTION_FOTOS_PRODUCTO.md`.
- Documentar proveedor reservado, borrado lógico, transición de inventario,
  procesamiento asíncrono y matriz de permisos.
- Distinguir comportamiento implementado de comportamiento aprobado pendiente.

### Verificación mínima

- No deben existir contradicciones entre documentos.
- Los criterios de aceptación deben cubrir los escenarios descritos en este plan.

---

## Tarea 2 — Estabilizar la verificación inicial

**Estado:** Completada (2 de septiembre de 2026)

Se corrigió la prueba raíz de Angular para verificar el `router-outlet` existente y
se configuró Maven Surefire para cargar Mockito 5.20 como `javaagent`, eliminando la
dependencia del self-attach. Se comprobó que el deadlock de esbuild pertenece al
sandbox: limitarlo a un worker no lo corrige, mientras el mismo build de producción
termina correctamente fuera del aislamiento; no fue necesario cambiar dependencias.

Verificación realizada: 72 pruebas unitarias backend sin Docker, suite backend
completa con 78 pruebas y PostgreSQL 16.2 mediante Testcontainers, 12 migraciones
Flyway aplicadas desde cero, 26 pruebas frontend y build Angular de producción. La
suite que usa Testcontainers y el build Angular requieren ejecución autorizada fuera
del sandbox en este entorno.

### Objetivo

Separar fallos reales de la funcionalidad de problemas preexistentes del entorno.

### Alcance

- Corregir la prueba Angular obsoleta.
- Configurar Mockito de forma compatible con Java 21 sin depender de self-attach.
- Verificar el acceso a Docker/Testcontainers o documentar el comando aprobado para
  ejecutar la integración.
- Reproducir y diagnosticar el deadlock de esbuild; corregir configuración o entorno
  sin actualizar dependencias mayores salvo aprobación adicional.

### Verificación mínima

- Backend compila.
- Pruebas unitarias pueden ejecutarse sin errores de infraestructura Mockito.
- Frontend compila y sus pruebas pasan.
- La imposibilidad de ejecutar Testcontainers, si permanece, queda documentada sin
  afirmar falsamente que la integración pasó.

---

## Tarea 3 — Catálogos obligatorios: categorías y proveedor reservado

**Estado:** Completada (2 de septiembre de 2026)

Se agregó la migración V13 con estado activo de proveedores, una identidad de sistema
única para `PENDIENTE` y protecciones de base de datos contra su modificación,
eliminación o uso en pagos. El backend resuelve esa identidad sin depender de un ID,
excluye el registro reservado de los selectores y búsquedas comerciales, valida y
pagina el mantenimiento de proveedores y aplica la matriz de roles aprobada.

La desactivación es lógica y reasigna en bloque todos los productos a `PENDIENTE`
dentro de la misma transacción. Las categorías normalizan y controlan nombres
duplicados y rechazan con 409 el borrado cuando existen productos o promociones
relacionados. Los errores de ausencia, validación, autorización e integridad ya no
exponen excepciones genéricas.

Verificación realizada: 85 pruebas unitarias sin Docker y suite completa de 97 pruebas
con PostgreSQL 16.2 mediante Testcontainers. Las 13 migraciones se aplicaron desde un
esquema limpio; V13 operó sobre proveedores y productos sembrados por migraciones
anteriores y una segunda ejecución de Flyway confirmó que no se duplica `PENDIENTE`.
La integración validó 403 por rol, protección de identidad y pagos, búsqueda/listado,
validaciones, reasignación, rollback y los recorridos administrador e inventarista
para crear categoría y usarla inmediatamente en un producto con cantidad desconocida.

### Objetivo

Dejar completos los dos catálogos obligatorios para crear un producto: categoría y
proveedor, permitiendo además que el proveedor comercial todavía no se conozca sin
debilitar la integridad referencial.

### Alcance

- Crear una migración Flyway nueva que agregue el estado activo requerido por el
  borrado lógico y cree de forma idempotente el proveedor `PENDIENTE`.
- Verificar el CRUD backend existente de categorías, sus validaciones, errores y
  permisos para `ADMINISTRADOR` e `INVENTARISTA`.
- Mantener disponible un listado de categorías apto para el selector de productos.
- Controlar nombres de categoría duplicados y el intento de eliminar una categoría
  relacionada, sin exponer errores internos.
- Proteger la unicidad de la identidad reservada sin depender de un ID fijo.
- Crear un componente de dominio/aplicación único para resolver el proveedor
  reservado; no duplicar búsquedas por nombre en distintos servicios.
- Excluirlo de pagos y de operaciones comerciales ordinarias.
- Al desactivar un proveedor, reasignar en bloque sus productos a `PENDIENTE` y marcar
  el proveedor inactivo dentro de la misma transacción.
- Sustituir excepciones genéricas por errores de dominio y respuestas HTTP adecuadas.
- Aplicar validaciones de nombre, RFC, teléfono, email, contacto y porcentaje.
- Añadir búsqueda, filtro por estado y paginación estable.
- Aplicar la matriz de autorización aprobada.

### Verificación mínima

- Migración completa sobre base limpia y sobre datos existentes.
- Existe exactamente un `PENDIENTE` tras reinicios o migraciones repetibles de prueba.
- No puede renombrarse, desactivarse ni recibir pagos.
- Desactivar un proveedor reasigna todos sus productos y nunca devuelve 409 por esa
  relación.
- Un error durante la reasignación revierte toda la operación.
- Roles no permitidos reciben 403.
- Un administrador o inventarista puede crear una categoría y usar inmediatamente su
  ID válido para crear un producto mediante la API.

---

## Tarea 4 — Contratos y casos de uso backend de productos

**Estado:** Completada (2 de septiembre de 2026)

Se separaron los contratos de alta y reemplazo completo, dejando fuera del control
del cliente el precio calculado, el stock y el estado activo. El código de barras se
normaliza y valida sin distinguir mayúsculas, y los duplicados producen 409. Las
relaciones y valores comerciales se validan en servicio; omitir el proveedor asigna
el registro reservado `PENDIENTE`, mientras un proveedor comercial inactivo se
rechaza.

Se añadieron operaciones explícitas de desactivación y reactivación sin borrado
físico. Las consultas del POS devuelven únicamente un DTO seguro, sin costos, y
excluyen inactivos por defecto para todos los roles; solo los roles administrativos
pueden solicitar expresamente el catálogo inactivo. La consulta combinada aplica en
una sola búsqueda los filtros de texto, categoría, proveedor, estado, precio y stock
bajo.

Verificación realizada: pruebas unitarias de creación, edición, proveedor reservado,
permisos y ciclo de vida; integración HTTP de validaciones, conflictos, visibilidad,
venta y persistencia de relaciones. El buscador Angular se adaptó al contrato seguro
por código de barras sin exponer el DTO administrativo.

### Objetivo

Completar el alta, edición, consulta, desactivación y reactivación segura de productos.

### Alcance

- Cuando `proveedorId` sea nulo en un alta o edición explícita, asignar `PENDIENTE`.
- Mantener obligatoria la relación JPA y la columna `productos.proveedor_id`.
- Separar contratos de creación y actualización si evita que una edición obligue a
  reenviar valores no modificados.
- Validar código de barras, nombre, descripción, categoría, costos, porcentaje,
  unidad de medida, stock mínimo y estado.
- Mapear proveedor reservado sin producir `null` ni romper listados o búsquedas.
- Implementar endpoints explícitos de desactivación/reactivación sin `DELETE` físico.
- Evitar que productos inactivos aparezcan en el POS por defecto.
- Aplicar la matriz de autorización.
- Mantener entidades JPA fuera de los contratos HTTP.

### Verificación mínima

- Alta con proveedor existente y alta sin proveedor seleccionado.
- Edición para asignar, cambiar o devolver el producto a `PENDIENTE`.
- Código de barras duplicado produce 409 controlado.
- Desactivación conserva relaciones históricas e impide venta.
- Reactivación restaura la disponibilidad administrativa.
- Pruebas de roles y validaciones de límites.

---

## Tarea 5 — Semántica segura del inventario desconocido

**Estado:** Completada (3 de septiembre de 2026)

Se actualizó `VentaService` para no validar ni descontar `stockActual` mientras el producto tenga `cantidadDesconocida = true`. Se configuró `InventarioService` para rechazar entradas y salidas relativas sobre productos con cantidad desconocida, exigiendo que la primera cuantificación sea un ajuste absoluto. En `ProductoService`, el ajuste absoluto acepta cero y valores positivos, rechaza valores negativos, establece el stock y desactiva la bandera `cantidadDesconocida = false` de forma atómica. Se introdujo bloqueo pesimista (`findByIdForUpdate` con `@Lock(LockModeType.PESSIMISTIC_WRITE)`) para evitar carreras de concurrencia. Se crearon y pasaron suites completas de pruebas unitarias e integración (`SemanticaInventarioIntegrationTest`).

---

## Tarea 6 — Pipeline asíncrono y seguro de fotografías

**Estado:** Completada (3 de septiembre de 2026)

Se implementó el pipeline asíncrono y seguro de fotografías cumpliendo todos los requisitos de seguridad y rendimiento:
1. **Persistencia y migración Flyway (`V14__foto_pipeline_asincrono.sql`)**: agregado de `estado_procesamiento` (`PENDIENTE`, `PROCESANDO`, `COMPLETADO`, `ERROR`), `mensaje_error` y `ruta_miniatura`.
2. **Seguridad y validación temprana (`ValidadorSeguridadImagen`)**: límite estricto de 4 MB (en `application.yml` y código), verificación de firmas binarias (magic bytes para JPEG, PNG y WEBP), rechazo explícito de SVG y secuencias multimarco, sanitización contra *path traversal*, e inspección previa a la decodificación completa de dimensiones (máx 4096×4096 px) y píxeles totales (máx 16 MP) previniendo bombas de descompresión.
3. **Ejecución asíncrona desacoplada (`AsyncConfig`, `ProductoFotoProcessor`)**: `ThreadPoolTaskExecutor` dedicado (`fotoProcessingExecutor`) con pool acotado (core 2, max 4, cola 50) y `CallerRunsPolicy`. Transacción `REQUIRES_NEW` independiente para no bloquear la confirmación de la subida ni leer datos no confirmados.
4. **Procesamiento de imagen**: normalización a máximo 512×512 manteniendo proporción, generación de miniatura 80×80 con recorte centrado (`Positions.CENTER`), recodificación sin metadatos (EXIF) y limpieza garantizada de archivos temporales.
5. **Endpoints y autorización (`ProductoController`, `ProductoFotoService`)**: respuesta HTTP `202 Accepted` al subir o reintentar foto, consulta de estado por ID, y protección de subida, eliminación y reintento restringida a `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA`.
6. **Validación automatizada**: suites unitarias completas (`ValidadorSeguridadImagenTest`, `ProductoFotoProcessorTest`) y ejecución exitosa de los 127 tests del backend.

---

Procesar imágenes sin bloquear la solicitud HTTP y sin exponer el servidor a archivos
maliciosos o consumo de recursos no acotado.

### Alcance

- Crear migración para estado de procesamiento, mensaje de error seguro y rutas de
  imagen normalizada/miniatura cuando sean necesarias.
- Configurar límite multipart de 4 MB y repetir la comprobación en aplicación.
- Inspeccionar firma, formato, dimensiones y cantidad total de píxeles antes de la
  decodificación completa.
- Rechazar archivos vacíos, truncados, con múltiples imágenes, formatos no permitidos
  y nombres inválidos.
- Guardar temporalmente la subida usando identificadores generados por el servidor.
- Configurar `@EnableAsync` y un `TaskExecutor` dedicado con pool, cola y política de
  rechazo acotados.
- Separar la transacción que registra `PENDIENTE` de la ejecución asíncrona para evitar
  que el worker lea datos todavía no confirmados.
- Normalizar a máximo 512 × 512 manteniendo proporción y generar miniatura 80 × 80 con
  recorte centrado.
- Recodificar sin metadatos y utilizar una extensión controlada por el formato real.
- Hacer idempotente el procesamiento para tolerar reintentos sin duplicar archivos.
- Limpiar temporales y resultados parciales tanto en éxito como en error.
- Exponer consulta de estado y permitir eliminar/reintentar errores de manera segura.
- Aplicar la matriz de autorización y evitar path traversal/IDOR.

### Verificación mínima

- 4 MB exactos se aceptan; más de 4 MB se rechaza con 413.
- JPEG y PNG válidos producen imagen máxima 512 × 512 y miniatura exacta 80 × 80.
- MIME o extensión falsificados, SVG, archivo truncado y bomba de dimensiones se
  rechazan.
- La solicitud devuelve 202 sin esperar el redimensionado.
- Saturar la cola no crea hilos ilimitados y produce una respuesta/estado controlado.
- Fallos no dejan archivos huérfanos ni una foto principal inconsistente.
- Un usuario no autorizado no puede subir, reemplazar, marcar ni eliminar imágenes.

---

## Tarea 7 — Exponer sesión y permisos al frontend

**Estado:** Completada (4 de septiembre de 2026)

Se expuso la sesión y permisos de forma segura y reactiva entre backend y frontend:
1. **Endpoint seguro de perfil en backend (`UsuarioPerfilDTO`, `UsuarioController`)**: Se implementó `GET /api/v1/usuarios/perfil` que entrega el perfil mínimo necesario (`id`, `username`, `nombre`, `apellidos`, `email`, `rol`, `tiendaId`, `tiendaNombre`) sin exponer datos confidenciales ni hashes de contraseñas.
2. **Modelado y estado reactivo mediante Signals (`AuthService`, `usuario-sesion.model.ts`)**: Modelado estricto con `RolUsuario` (`ADMINISTRADOR`, `GERENTE`, `INVENTARISTA`, `VENDEDOR`) y `UsuarioSesion`. Signals reactivos (`currentUser`, `userRole`, `isAdmin`, `isGerente`, `isInventarista`, `isVendedor`, y métodos de evaluación como `canAccessAdmin()`, `canManageProducts()`, `canManageProviders()`, `canDeactivateProviders()`, `canManageUsers()`).
3. **Flujo de login y persistencia desacoplada**: `AuthService.login()` autentica y encadena la carga del perfil vía `GET /api/v1/usuarios/perfil`. Almacenamiento seguro en `sessionStorage` y limpieza exhaustiva en `logout()`.
4. **Protección de rutas por roles (`roleGuard`)**: Guard funcional que verifica roles requeridos, redirigiendo a `/login` si no está autenticado o a `/pos` si el rol no tiene acceso al área requerida.
5. **Interceptor de errores HTTP centralizado (`errorInterceptor`)**: Captura errores `401 Unauthorized` de peticiones autenticadas para forzar el logout y redirección inmediata a `/login` sin afectar la pantalla de login. Registrado en `app.config.ts`.
6. **Verificación automatizada**:
   - Backend: Prueba de integración en `SemanticaInventarioIntegrationTest` validando `GET /api/v1/usuarios/perfil` para distintos roles y rechazo anónimo (128 tests pasando exitosamente).
   - Frontend: 33 tests pasando exitosamente (`auth.service.spec.ts`, `role.guard.spec.ts`, `error.interceptor.spec.ts`, etc.) y build de producción limpio sin errores (`ng build`).

---

Permitir navegación administrativa coherente con la autorización real del backend.

### Alcance

- Exponer en login o en un endpoint de perfil el identificador, nombre, rol y tienda
  mínimos necesarios.
- No exponer datos sensibles ni utilizar claims sin validar como autoridad backend.
- Mantener el estado de sesión mediante Signals.
- Crear guard por roles y tratamiento común de 401/403.
- Proteger rutas administrativas sin afectar el POS existente.

### Verificación mínima

- Cada rol recibe únicamente la información necesaria.
- Manipular estado del frontend no concede acceso backend.
- Sesión expirada limpia estado y redirige de forma consistente.

---

## Tarea 8 — Área administrativa y navegación

**Estado:** Completada (4 de septiembre de 2026)

Se implementó el contenedor frontend administrativo y la navegación unificada:
1. **Barra superior transversal compartida (`TopNavBarComponent`)**:
   - Creado componente standalone reutilizable en todas las pantallas (`/pos`, `/admin`, `/perfil`).
   - Muestra el nombre del usuario, su rol asignado (`ADMINISTRADOR`, `GERENTE`, `INVENTARISTA`, `VENDEDOR`) y nombre de la sucursal/tienda.
   - Enlace contextual dinámico al POS y botón "Administración" visible únicamente si el usuario tiene rol con permisos administrativos (`ADMINISTRADOR`, `GERENTE`, `INVENTARISTA`).
   - Menú de acordeón desplegable en la esquina superior derecha con opciones para ir a "Mi Perfil (Editar datos y clave)", acceso rápido a "Punto de Venta", acceso a "Panel de Administración" y botón seguro de "Cerrar sesión".
2. **Layout y navegación administrativa responsiva (`AdminLayoutComponent`, `AdminDashboardComponent`)**:
   - Barra lateral desktop/tablet con filtrado reactivo de módulos según el rol (Productos, Categorías, Proveedores, Inventario y Usuarios).
   - Barra móvil/tablet con scroll horizontal para pantallas reducidas.
   - Dashboard de bienvenida con tarjetas interactivas de módulos administrativos.
   - Enlace directo de regreso al POS integrado en el layout.
3. **Rutas protegidas granulares (`app.routes.ts`)**:
   - Ruta base `/admin` protegida con `roleGuard(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA'])`.
   - Sub-rutas hijas `/admin/productos`, `/admin/categorias`, `/admin/proveedores`, `/admin/inventario` y `/admin/usuarios` protegidas con sus roles correspondientes mediante `roleGuard`.
   - Ruta `/perfil` protegida con `authGuard`.
4. **Verificación y pruebas**:
   - Pruebas unitarias completas: `admin-layout.spec.ts`, `top-nav-bar.component.spec.ts`.
   - Suite total de pruebas frontend: **38 tests pasados con éxito, 0 fallos**.
   - Build de producción (`ng build`) completado sin advertencias ni errores.

---

Crear el contenedor frontend para productos, proveedores e inventario.

### Alcance

- Añadir ruta y layout standalone protegidos.
- Incorporar navegación responsive y acciones visibles según rol.
- Reutilizar estilos y componentes compartidos existentes sin modificar trabajo no
  relacionado.
- Manejar carga inicial, acceso denegado, error y navegación de regreso al POS.

### Verificación mínima

- Acceso y opciones correctas para los cuatro roles.
- Funcionamiento usable en escritorio y tablet.
- Compilación y pruebas de navegación.

---

## Tarea 9 — Interfaces de categorías y proveedores

**Estado:** Completada (4 de septiembre de 2026)

Se desarrollaron y verificaron de extremo a extremo las interfaces de gestión de categorías y proveedores conectadas con el backend:
1. **Modelos y contratos (`categoria.model.ts`, `proveedor.model.ts`)**:
   - Modelado de categoría con ID opcional para creación, nombre y descripción.
   - Modelado de proveedor con soporte de RFC, porcentaje de comisión, teléfono, contacto, email, flags de `activo` y proveedor protegido de `sistema` (`PENDIENTE`).
2. **Servicios HTTP reactivos (`CategoriaService`, `ProveedorService`)**:
   - `CategoriaService`: `listarTodas()`, `obtenerPorId()`, `crear()`, `actualizar()` y `eliminar()` apuntando a `/api/v1/categorias`.
   - `ProveedorService`: `listarTodos()`, `buscar(termino, activo, page, size)` con mapeo del formato PageDTO del backend, `obtenerPorId()`, `crear()`, `actualizar()`, `desactivar()` y `contarProductosAsignados(id)`.
   - **Extensión de Backend**: Se agregó `contarProductosAsignados` en `ProveedorService` y el endpoint `GET /api/v1/proveedores/{id}/productos/conteo` en `ProveedorController` para conocer la cantidad exacta de productos que serán reasignados al proveedor del sistema antes de confirmar la desactivación.
3. **Componente de administración de categorías (`CategoriasAdminComponent`)**:
   - Listado reactivo con conteo total y diseño en tarjetas limpias.
   - Modal de creación y edición con validaciones (`nombre` obligatorio hasta 255 caracteres, `descripcion` opcional hasta 500 caracteres).
   - Acceso seguro condicionado a `ADMINISTRADOR` e `INVENTARISTA` mediante `authService.canManageProducts()`.
   - Manejo de estados de carga, vacío, alertas de éxito y reporte de errores backend.
4. **Componente de administración de proveedores (`ProveedoresAdminComponent`)**:
   - Tabla paginada completa con búsqueda interactiva (por nombre, RFC o contacto) y filtro por estado (todos, solo activos, solo inactivos).
   - Badge distintivo para proveedor de sistema `PENDIENTE`, bloqueando edición o desactivación sobre el mismo.
   - Formulario modal para alta y edición con validación de RFC mexicano vía regex, porcentaje de comisión (0 a 100), formato de teléfono y correo electrónico.
   - Flujo de desactivación segura exclusivo para `ADMINISTRADOR` (`authService.canDeactivateProviders()`): diálogo modal que consulta e informa la cantidad de productos asociados que serán transferidos automáticamente al proveedor `PENDIENTE`.
   - Alta y edición disponibles para `ADMINISTRADOR` y `GERENTE` (`authService.canManageProviders()`).
5. **Rutas y navegación integrada**:
   - Conectadas las rutas `/admin/categorias` y `/admin/proveedores` bajo `AdminLayoutComponent` en `app.routes.ts`.
6. **Verificación y pruebas**:
   - Frontend: 15 archivos de pruebas y 54 pruebas unitarias pasando con Vitest (`proveedores-admin.component.spec.ts`, `categorias-admin.component.spec.ts`, `proveedor.service.spec.ts`, `categoria.service.spec.ts`). Build de producción Angular (`ng build`) completado con éxito generando chunks independientes `categorias-admin-component` y `proveedores-admin-component`.
   - Backend: Pruebas unitarias de `ProveedorServiceTest` y suite completa de integración pasando exitosamente con base de datos PostgreSQL.

### Objetivo

Administrar las dos relaciones obligatorias del producto y representar claramente el
estado de proveedor pendiente.

### Alcance

- Crear modelos y servicio HTTP basados en contratos backend verificados.
- Implementar listado, alta y edición de categorías para `ADMINISTRADOR` e
  `INVENTARISTA`.
- Permitir volver al formulario de producto y seleccionar inmediatamente una
  categoría recién creada.
- Implementar listado paginado, búsqueda y filtro activo/inactivo.
- Implementar alta, edición y desactivación según permisos.
- Mostrar el número de productos que serán reasignados antes de desactivar cuando el
  backend proporcione ese dato.
- Ocultar `PENDIENTE` de la administración ordinaria y mostrarlo solo como estado del
  sistema.
- Manejar validaciones, carga, vacío, 400, 403, 404 y errores de conexión.

### Verificación mínima

- Flujo completo contra backend real.
- Un administrador o inventarista puede crear y seleccionar una categoría sin usar
  herramientas externas.
- Desactivación actualiza la lista y los productos quedan pendientes.
- Un rol de solo lectura no puede ejecutar escrituras aunque manipule la UI.

---

## Tarea 10 — Catálogo y formulario de productos

**Estado:** Completada (4 de septiembre de 2026)

Se implementó y verificó de forma integral el catálogo y formulario de productos en el área administrativa, conectado con contratos verificados del backend:
1. **Modelos y contratos (`producto.model.ts`)**:
   - Modelado completo de `ProductoDetalle`, `ProductoCrearRequest`, `ProductoActualizarRequest`, `ProductoFiltros`, `ProductoListado` y `Pagina<T>`.
2. **Servicio HTTP frontend (`ProductoService`)**:
   - Métodos `buscarAvanzado` (filtros combinados por término, categoría, proveedor, estado y stock bajo), `obtenerPorId`, `crear`, `actualizar`, `desactivar` y `reactivar`.
   - Pruebas unitarias completas en `producto.service.spec.ts` (9 tests pasando con 100% de cobertura de métodos).
3. **Robustez y compatibilidad de consultas backend (`ProductoRepository`, `ProductoService`)**:
   - Se ajustó `buscarProductos` para recibir un `:patron` preformateado en minúsculas con comodines en lugar de ejecutar `LOWER(CONCAT('%', :termino, '%'))`, garantizando compatibilidad total con PostgreSQL y eliminando fallos 500 (`lower(bytea)`) cuando `termino` no viene informado.
   - Nueva prueba de integración en `CatalogosIntegrationTest` validando búsquedas sin término y con filtros combinados contra PostgreSQL real en Testcontainers.
4. **Componente de administración de catálogo (`ProductosAdminComponent`)**:
   - Listado responsivo con tarjetas/tabla paginada: foto/miniatura, código de barras, nombre, categoría, proveedor (o badge `PENDIENTE`), precio de venta oficial del backend, existencia (con badge "Por contar" para productos con `cantidadDesconocida = true` y alerta de stock bajo cuando `stockActual <= stockMinimo`), y estado activo/inactivo.
   - Filtros combinados: búsqueda por texto/código, categoría, proveedor, estado (activos, inactivos, todos) y condición de inventario (todos, stock bajo, por contar).
   - Formulario modal de alta y edición con validaciones reactivas estrictas:
     - Código de barras normalizado en mayúsculas (regex `^[A-Za-z0-9._-]+$`).
     - Categoría obligatoria con selector alimentado por `CategoriaService`.
     - Proveedor opcional en la interfaz; si no se selecciona, el backend asigna automáticamente el proveedor del sistema `PENDIENTE`.
     - Costo de compra validado (> 0 con precisión decimal).
     - Margen de ganancia manual editable para `ADMINISTRADOR` y `GERENTE`, y bloqueado informativamente para `INVENTARISTA` (calculado por escala del sistema).
     - Opción explícita "Cantidad aún no contabilizada" (`cantidadDesconocida = true`) sin pedir existencia inicial.
     - Indicador informativo de que el precio de venta es calculado de forma fidedigna por el backend.
   - Diálogo de confirmación para desactivación y reactivación segura conforme a la matriz de permisos (`ADMINISTRADOR` y `GERENTE`).
5. **Rutas e Integración**:
   - Ruta `/admin/productos` conectada a `ProductosAdminComponent` en `app.routes.ts`.
6. **Verificación y Pruebas**:
   - Frontend: 16 archivos de prueba y 67 tests unitarios pasando exitosamente (`npm test -- --watch=false`). Build de producción completado con éxito (`chunk-B-gwvvia.js`).
   - Backend: 130 tests unitarios y de integración pasando sin errores en `./mvnw test`.

### Objetivo

Permitir dar de alta, localizar y editar productos sin detener la tienda.

### Alcance

- Crear listado paginado con búsqueda y filtros por estado, categoría, proveedor y
  condición de inventario.
- Mostrar foto, código, nombre, categoría, proveedor o estado pendiente, precio,
  existencia y estado activo.
- Implementar formulario de alta y edición con categoría obligatoria y proveedor
  opcional en UI.
- Incluir una opción explícita “Cantidad aún no contabilizada”.
- No pedir cantidad inicial cuando esa opción esté activa.
- Mostrar precio calculado por backend; no confiar en cálculos del navegador.
- Implementar desactivación/reactivación conforme a permisos.
- Manejar códigos duplicados y validaciones del backend.

### Verificación mínima

- Alta con proveedor, sin proveedor y con cantidad desconocida.
- Alta usando una categoría creada desde la misma área administrativa.
- Edición de proveedor, costo, margen, categoría, stock mínimo y estado.
- Productos inactivos no aparecen en el buscador POS.
- El frontend no inventa campos ni reglas ausentes del backend.

---

## Tarea 11 — Interfaz de inventario y fotografías

**Estado:** Completada (4 de septiembre de 2026)

Se implementó de manera completa la interfaz de control de inventario y la galería modal
de fotografías de productos:
1. **Control de inventario (`/admin/inventario`):**
   - Tarjeta destacada de atención para productos con cantidad desconocida ("Por contar"), con
     acción rápida "Conteo inicial" que abre el formulario de ajuste en modo absoluto (`esFijarStockAbsoluto = true`)
     para fijar la existencia real y apagar automáticamente `cantidadDesconocida`.
   - Registro de movimientos manuales de Entrada (validación cantidad > 0, motivo obligatorio y costo de compra),
     Salida (validación cantidad > 0, motivo obligatorio y comprobación contra existencia disponible) y
     Ajuste de Stock (soporte de conteo físico absoluto o ajuste relativo con motivo obligatorio).
   - Tabla paginada de historial de movimientos con filtros por tipo de movimiento, producto y fechas,
     y ocultamiento dinámico de costos unitarios cuando el usuario tiene rol `VENDEDOR`.
2. **Galería modal y pipeline asíncrono de fotografías:**
   - Carga de imágenes con validación cliente de tamaño exacto (máximo 4 MB / 4,194,304 bytes) y tipos MIME permitidos (`image/jpeg`, `image/png`).
   - Envío multipart `POST /api/v1/productos/{id}/fotos` que recibe respuesta `202 Accepted`.
   - Polling reactivo acotado (intervalo de 1.5s, máximo 15 intentos) para consultar estado asíncrono hasta alcanzar `COMPLETADO` o `ERROR`, con cancelación inmediata al destruir el componente.
   - Indicadores visuales de estado (`PENDIENTE`, `PROCESANDO`, `LISTA`, `ERROR` con mensaje descriptivo), opción de reintentar procesamiento fallido, marcado de foto principal y eliminación.
   - Botón directo de "Fotografías" en el catálogo de productos (`/admin/productos`), con recarga reactiva de miniaturas.
3. **Verificación técnica:**
   - 90 pruebas unitarias frontend pasando (20 archivos de prueba).
   - Build de producción Angular generado exitosamente sin advertencias.
   - 130 pruebas unitarias y de integración de backend pasando.

### Objetivo

Completar el conteo gradual, los movimientos y la administración visual del producto.

### Alcance

- Implementar acción destacada “Registrar conteo inicial” para cantidad desconocida.
- Implementar entrada, salida y ajuste con motivo obligatorio para cantidad conocida.
- Consultar historial paginado y ocultar acciones/costos conforme al rol.
- Cargar imágenes con validación cliente de 4 MB como ayuda, manteniendo backend como
  autoridad.
- Mostrar progreso de transferencia y estado asíncrono de procesamiento.
- Consultar estado con polling acotado y detenerlo al salir, completar o fallar.
- Mostrar preview/placeholder, seleccionar principal y eliminar/reintentar.
- Refrescar catálogo y POS después de cambios confirmados.

### Verificación mínima

- El negocio puede vender otros productos mientras se cuenta el catálogo gradualmente.
- El producto contado cambia inmediatamente al control normal de stock.
- La UI representa `PENDIENTE`, `PROCESANDO`, `LISTA` y `ERROR` sin loops infinitos.
- Subir, procesar, marcar principal y eliminar funciona contra backend real.

---

## Tarea 12 — QA integral, seguridad y cierre documental

**Estado:** Pendiente

### Objetivo

Verificar el flujo completo y sincronizar documentación con el comportamiento real.

### Alcance

- Ejecutar pruebas unitarias, API, persistencia, frontend e integración.
- Aplicar revisión de arquitectura, backend, frontend, seguridad y QA.
- Probar matriz de roles por endpoint, incluyendo intentos directos sin UI.
- Verificar migraciones desde V1 sobre PostgreSQL limpio y sobre una copia con datos.
- Probar concurrencia de ventas, conteos y movimientos.
- Probar límites y archivos hostiles del pipeline de imágenes.
- Verificar que no existan archivos huérfanos ni productos/proveedores borrados
  físicamente.
- Actualizar requerimientos y este plan con resultados, fecha y pendientes reales.

### Verificación mínima

- Flujo real: crear proveedor → crear producto → cargar foto → procesar → vender con
  cantidad desconocida → contar → vender con control de stock → editar → desactivar.
- Flujo real alterno: crear producto sin proveedor → asignación a `PENDIENTE` → asignar
  proveedor → desactivar proveedor → reasignación automática a `PENDIENTE`.
- Flujo por rol: iniciar sesión como `ADMINISTRADOR` y como `INVENTARISTA` → crear o
  seleccionar categoría → crear producto con proveedor o `PENDIENTE` → consultar el
  producto recién creado en el catálogo administrativo.
- Backend y frontend compilan y sus pruebas aprobadas pasan.
- No quedan TODO esenciales ni contratos temporales.

## Orden obligatorio de ejecución

```text
T1 Documentación
      ↓
T2 Estado base verificable
      ↓
T3 Categorías y proveedor reservado
      ↓
T4 Backend de productos
      ↓
T5 Inventario desconocido
      ↓
T6 Fotografías asíncronas y seguras
      ↓
T7 Sesión y permisos frontend
      ↓
T8 Área administrativa
      ↓
T9 Categorías y proveedores frontend
      ↓
T10 Productos frontend
      ↓
T11 Inventario y fotos frontend
      ↓
T12 QA y cierre
```

El frontend no debe comenzar hasta que los contratos backend requeridos estén
implementados, verificados, revisados y guardados por el propietario.

## Requerimientos relacionados

- `docs/requerimientos/productos/CRUD_PRODUCTOS.md`
- `docs/requerimientos/productos/BUSQUEDA_PRODUCTOS.md`
- `docs/requerimientos/productos/AJUSTE_STOCK_MINIMO.md`
- `docs/requerimientos/productos/GESTION_FOTOS_PRODUCTO.md`
- `docs/requerimientos/categorias/CRUD_CATEGORIAS.md`
- `docs/requerimientos/inventario/CANTIDAD_DESCONOCIDA.md`
- `docs/requerimientos/inventario/MOVIMIENTO_INVENTARIO.md`
- `docs/requerimientos/inventario/STOCK_POR_TIENDA.md`
- `docs/requerimientos/proveedores/CRUD_PROVEEDORES.md`
- `docs/requerimientos/proveedores/REPORTE_COMISIONES_PROVEEDOR.md`
- `docs/requerimientos/seguridad/PROTECCION_XSS.md`
- `docs/requerimientos/calidad/ESTRATEGIA_PRUEBAS.md`
- `docs/requerimientos/calidad/PRECISION_MONETARIA.md`
