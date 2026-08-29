# CLAUDE.md — Frontend Papelería Patata

Contexto para sesiones de Claude Code trabajando **solo en `frontend/`**. El backend lo
lleva otro agente en paralelo — no edites nada bajo `backend/`; si necesitas un endpoint
que no existe o ves un contrato distinto al descrito aquí, repórtalo en vez de tocar el
backend.

## Qué es esto

POS (punto de venta) para una papelería. Angular SPA que consume la API REST de
`backend/` (Spring Boot). Dos superficies:
1. **Home pública** (sin login): escaparate del negocio.
2. **POS + administración** (requiere login JWT): venta rápida, catálogo, inventario,
   clientes, proveedores.

Fuentes de verdad del producto (léelas si falta contexto):
- `frontend/docs/roadmap.md` — alcance funcional del MVP alfa.
- `docs/planeacion_tareas.md` (raíz del repo) — lista de tareas secuenciales; vamos
  tarea por tarea, con pausas para revisión/commit entre cada una.
- `backend/docs/avance-mvp.md` — estado del backend. **Puede estar desactualizado**:
  a fecha de este documento, `ClienteController` y `ProveedorController` ya tienen CRUD
  completo aunque el reporte los liste como pendientes. Ante duda, verifica el código
  real en `backend/src/main/java/.../{cliente,proveedor}/*Controller.java` en vez de
  confiar ciegamente en el doc.

## Stack y decisiones ya tomadas

- **Angular 22**, standalone components (sin NgModules), CLI generado con
  `--routing --style=css --ssr=false` (SPA pura, sin SSR — coincide con el roadmap).
- **Zoneless**: no hay `zone.js` en el proyecto (scaffold por defecto de Angular 22).
  Usa `signals` para estado reactivo; no asumas que un cambio de propiedad plano
  dispara detección de cambios.
- **TailwindCSS v4**, integrado vía PostCSS (`.postcssrc.json` +
  `@import "tailwindcss";` en `src/styles.css`). No hay `tailwind.config.js` — Tailwind
  v4 se configura mayormente vía CSS (`@theme` en `styles.css` si hace falta extender
  tokens de diseño).
- **Testing**: `ng test` corre sobre **Vitest** (no Karma/Jasmine — así viene el
  scaffold de Angular 22).
- **HTTP**: `provideHttpClient(withFetch(), withInterceptors([]))` ya está en
  `src/app/app.config.ts`. El array de interceptores está vacío a propósito — el
  interceptor de JWT (Tarea 3 de la planeación) va en `core/interceptors/` y se
  registra ahí.
- Node global actualizado a v24.19.0 (vía `brew upgrade node@24`) porque el Angular
  CLI más reciente lo exige. Si `ng` se queja de versión de Node, es un problema de
  entorno, no del proyecto.

## Estructura de carpetas

```
src/app/
  core/            # singletons: guards, interceptors, services, models compartidos
    guards/
    interceptors/
    services/
    models/
  shared/          # UI reutilizable sin lógica de negocio propia
    components/
    pipes/
    directives/
  features/        # un subdirectorio por feature (home, auth, pos, productos, ...)
```

Las carpetas están vacías por ahora (con `.gitkeep`) — se van poblando conforme
avanzan las tareas de `docs/planeacion_tareas.md`. Sigue esta convención en vez de
crear estructuras nuevas.

## Contrato de la API (backend)

- Base URL dev: `http://localhost:8080/api/v1` (backend no define `server.port`,
  usa el 8080 por defecto de Spring Boot). El CORS del backend (`SecurityConfig`)
  solo permite origin `http://localhost:4200` — es decir, `ng serve` debe correr en
  el puerto por defecto de Angular.
- **Auth** (`/api/v1/auth`, público):
  - `POST /login` — body `{ username, password }` → `AuthResponse`.
  - `POST /register` — solo rol `ADMINISTRADOR`.
  - `POST /refresh-token` — token viejo en header `Authorization: Bearer <token>`.
  - `AuthResponse`: `{ accessToken, refreshToken, requiereCambioPassword }`. Si
    `requiereCambioPassword` es `true`, el flujo de login debe forzar cambio de
    contraseña antes de dejar entrar al POS.
- **Roles** (`RolUsuario`): `ADMINISTRADOR`, `GERENTE`, `VENDEDOR`, `INVENTARISTA`.
  Reglas de acceso relevantes para guards/UI condicional:
  - `/api/v1/ventas/**` — solo `ADMINISTRADOR` y `VENDEDOR`.
  - `/api/v1/productos/**`, `/api/v1/categorias/**` — cualquier usuario autenticado
    (la escritura fina se controla con `@PreAuthorize` en el backend, no siempre
    visible desde las rutas).
  - `/api/v1/admin/**` — solo `ADMINISTRADOR`.
- **Errores**: formato uniforme `ErrorResponse { status, error, mensaje, timestamp }`
  vía `@ControllerAdvice` global. Maneja este shape en el manejo de errores HTTP del
  frontend (interceptor o servicio central), no asumas mensajes libres.
- **Paginación**: Spring configurado con
  `spring.data.web.pageable.serialization-mode: via-dto` — las respuestas paginadas
  vienen como un DTO explícito (no la serialización cruda de `Page<T>` de Spring Data,
  que expondría internals de Hibernate). Parámetros de query: `page`, `size` (máx 100,
  default 20), `sort`.
- **Endpoints CRUD confirmados en código** (verifica siempre contra el controller
  real si vas a integrar uno, esto es un resumen):
  - `ProductoController` (`/api/v1/productos`): crear, `PUT /{id}`, `GET /{id}`,
    `GET /buscar` (paginado, filtros), `GET /codigo/{codigoBarras}`,
    `GET /categoria/{categoriaId}`, `GET /proveedor/{proveedorId}`,
    `POST /ajustar-inventario`, `GET /stock-bajo`, endpoints de fotos
    (`/{productoId}/fotos/**`).
    - ⚠️ **Gap conocido**: `ProductoRequestDTO` sí acepta `cantidadDesconocida`, pero
      `ProductoResponseDTO` **no lo expone** en la respuesta. Si el POS necesita saber
      si un producto tiene cantidad desconocida (para no bloquear venta por stock),
      probablemente haya que pedirle al agente de backend que lo agregue al DTO de
      respuesta antes de poder consumirlo bien desde el frontend.
  - `VentaController` (`/api/v1/ventas`): **solo `POST /`** (crear venta) implementado.
    No hay todavía `GET` de listado, detalle, ventas del día ni cancelación — el POS no
    podrá mostrar historial hasta que el backend los exponga (ver Tarea 6 de
    `docs/planeacion_tareas.md`).
  - `ClienteController` (`/api/v1/clientes`): CRUD completo (`GET`, `GET /{id}`,
    `POST`, `PUT /{id}`, `DELETE /{id}`).
  - `ProveedorController` (`/api/v1/proveedores`): CRUD completo, mismo patrón.
  - `CategoriaController`, `TiendaController`, `UsuarioController`,
    `InventarioController`: CRUD/operaciones completas, ver `avance-mvp.md` sección 1.4
    para detalle (esa parte del reporte sí está al día).

## Convenciones de código Angular

- Componentes **standalone**, un componente por carpeta bajo su feature
  (`nombre.ts` / `nombre.html` / `nombre.css`), sin sufijo `Component` en el nombre
  de archivo (así generó el CLI: `app.ts`, no `app.component.ts` — sigue ese patrón
  para componentes nuevos).
- Usa `inject()` en vez de inyección por constructor cuando tenga sentido (estilo
  moderno de Angular), pero constructor injection también es válido — prioriza
  consistencia dentro de un mismo archivo/feature más que una regla estricta.
- Preferir `signals` (`signal`, `computed`, `input()`, `output()`) sobre
  `@Input()`/`@Output()` decorators y sobre RxJS para estado local simple. RxJS sigue
  siendo la herramienta correcta para streams async (HTTP, eventos con debounce, etc.).
- Control flow nativo (`@if`, `@for`, `@switch`) en templates, no `*ngIf`/`*ngFor`.
- Nombres, mensajes de UI y comentarios en español (coherente con el dominio del
  negocio y el backend). Nombres de variables/funciones en inglés o español según lo
  que ya predomine en el archivo que edites.

## Comandos

```bash
npm start          # ng serve, puerto 4200 (requerido por CORS del backend)
npm run build       # build de producción a dist/frontend
npm run watch       # build en modo desarrollo con watch
npm test            # vitest vía ng test
```

El backend se levanta aparte (ver `backend/AGENTS.md` / README del backend); asume
que ya está corriendo en `localhost:8080` para desarrollo local, no lo arranques tú.

## Notas para próximas sesiones

- Vamos avanzando tarea por tarea según `docs/planeacion_tareas.md`, con pausas para
  revisión y commit — no adelantes varias tareas de golpe sin confirmar.
- Este archivo es el punto de entrada de contexto del frontend; actualízalo cuando se
  tomen decisiones nuevas de arquitectura/estilo/contrato de API que otra sesión
  necesitaría conocer (no lo uses como bitácora de cambios línea por línea, para eso
  está git log).
