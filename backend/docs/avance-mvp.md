# Reporte de Avance — Backend POS Papelería

**Fecha**: 22 de mayo de 2026
**Stack**: Spring Boot 4.0.2, Java 21, PostgreSQL/H2, JWT, Flyway, Docker
**Objetivo**: Evaluar el avance real del backend y estimar lo que falta para un MVP funcional en servidor.

---

## Resumen Ejecutivo

**Avance estimado**: ~35-40% del MVP.

El proyecto tiene una base arquitectónica sólida (Spring Boot, seguridad JWT, entidades bien modeladas), pero la capa de negocio está incompleta. El módulo de **Productos** es el más maduro (~85%). Los módulos de **Ventas** y **Auth** tienen funcionalidad parcial con bugs críticos. Los módulos de **Categorías, Clientes, Tiendas, Proveedores, Usuarios e Inventario** carecen de servicios y controladores. No hay reportes, corte de caja, ni manejo de devoluciones.

---

## 1. Lo que YA existe (implementado)

### 1.1 Infraestructura
| Componente | Estado | Detalle |
|---|---|---|
| Spring Boot 4.0.2 + Java 21 | ✅ | Virtual Threads habilitados |
| PostgreSQL (prod) / H2 (dev) | ✅ | Perfiles dev y prod configurados; H2 en modo PostgreSQL |
| Dockerfile multi-etapa | ✅ | Build con Temurin 21, ZGC en runtime, usuario no-root |
| Flyway | ✅ | V1: esquema inicial (12 tablas + índices), V2: datos de prueba |
| CORS configurado | ✅ | Permite localhost:4200 |
| OpenAPI / Swagger UI | ✅ | Dependencia springdoc incluida |
| Lombok | ✅ | |

### 1.2 Entidades (12 entidades)
| Entidad | Tabla | Relaciones | Estado |
|---|---|---|---|
| `Producto` | `productos` | Categoria, Proveedor, Fotos | ✅ Completo, con `@PrePersist` para cálculo de precio |
| `Categoria` | `categorias` | Productos | ✅ |
| `Venta` | `ventas` | Usuario, Tienda, Cliente, Detalles | ✅ |
| `DetalleVenta` | `detalles_venta` | Venta, Producto | ✅ |
| `Cliente` | `clientes` | Ventas, Promociones | ✅ Incluye factory para cliente anónimo |
| `PromocionCliente` | `promociones_cliente` | Cliente | ✅ |
| `Usuario` | `usuarios` | Tienda | ✅ Implementa `UserDetails` |
| `Tienda` | `tiendas` | Usuarios, Ventas | ✅ |
| `Proveedor` | `proveedores` | Productos, Pagos | ✅ |
| `PagoProveedor` | `pagos_proveedor` | Proveedor | ✅ |
| `InventarioMovimiento` | `inventario_movimientos` | Producto, Usuario | ✅ |
| `ProductoFoto` | `producto_fotos` | Producto | ✅ Con soporte para thumbnail |

### 1.3 Seguridad
| Componente | Estado |
|---|---|
| JWT (jjwt 0.12.5) — generación, validación, refresh | ✅ |
| Filtro `JwtAuthFilter` | ✅ |
| `SecurityConfig` con roles | ✅ |
| `UserDetailServiceImpl` | ✅ |
| BCrypt password encoder | ✅ |
| Data Seeder (admin + cashier) | ✅ |

### 1.4 Módulos con funcionalidad implementada

#### Productos (~85% completo)
- **Controller** (`/api/productos`): ✅
  - CRUD: crear, actualizar, obtener por ID
  - Búsqueda avanzada con paginación (por término, categoría, proveedor, precio, stock bajo)
  - Búsqueda por código de barras
  - Listado por categoría y por proveedor
  - Ajuste de inventario
  - Subida/descarga/eliminación de fotos con thumbnails
  - Foto principal
  - Anotaciones OpenAPI completas
- **Service**: ✅ 355 líneas con lógica de negocio
  - Cálculo automático de porcentaje de ganancia por rangos de costo
  - Control de permisos por rol (inventarista, gerente, admin)
  - Validación de código de barras duplicado
- **Repository**: ✅ 10+ queries personalizadas (JPQL, JOIN FETCH)
- **DTOs**: ✅ Request, Response, Listado, Detalle, Búsqueda

#### Ventas (~30% completo)
- **Controller** (`/api/v1/ventas`): ⚠️ Solo POST (crear venta). Sin anotaciones Swagger.
- **Service**: ⚠️ Lógica parcial
  - ✅ Generación de folio
  - ✅ Manejo de cliente anónimo
  - ✅ Validación de descuentos por rol
  - ❌ **BUG CRÍTICO**: No mapea los detalles de la venta desde el DTO. `venta.getDetalles()` siempre está vacío.
  - ❌ `verificarPromocionesCliente` crea la promoción pero nunca la guarda.
  - ❌ No hay GET para listar/consultar ventas.
  - ❌ No hay endpoint para cancelar ventas.
  - ❌ No hay endpoint para ventas del día.
- **Repository**: ✅ 8 queries (ventas del día, por proveedor, estadísticas anónimas/registradas)

#### Auth (~70% completo)
- **Controller** (`/api/v1/auth`): ✅ Register, login, refresh-token
- **Service**:
  - ✅ Registro con encoding de password
  - ✅ Login con autenticación Spring Security
  - ✅ Refresh token
  - ❌ Registro siempre asigna rol `VENDEDOR`, ignora `request.getRole()`
  - ❌ Login usa `request.email()` pero `loadUserByUsername` busca por `username`
- **DTOs**: ✅ LoginRequest (record), RegisterRequest (Lombok), AuthResponse (record)

#### Almacenamiento de archivos (~90% completo)
- ✅ `FileSystemStorageService` con protección Path Traversal
- ✅ `StorageService` interfaz
- ✅ `ByteArrayMultipartFile` para thumbnails en memoria
- ✅ `ProductoFotoService` (subir, eliminar, listar, principal, descargar con thumbnail)

### 1.5 Enums
| Enum | Valores |
|---|---|
| `RolUsuario` | ADMINISTRADOR, GERENTE, VENDEDOR, INVENTARISTA |
| `EstadoVenta` | COMPLETADA, CANCELADA |
| `MetodoPago` | EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA |
| `TipoMovimiento` | ENTRADA, SALIDA, AJUSTE |

### 1.6 Manejo de errores
- ✅ `GlobalExceptionHandler` con handlers para `ResourceNotFoundException` y `AccesoDenegadoException`
- ✅ `ErrorResponse` DTO estructurado
- ❌ Sin handler para `IllegalArgumentException` ni `MethodArgumentNotValidException` (validación)

### 1.7 Migraciones Flyway
| Versión | Contenido |
|---|---|
| V1 | Esquema inicial: 12 tablas, secuencia `seq_folio_venta`, 12 índices |
| V2 | Datos de prueba: 1 tienda, 2 usuarios, 4 categorías, 2 proveedores, 3 clientes, 10 productos, 3 ventas |

---

## 2. Bugs y problemas críticos detectados

### 🔴 Críticos (bloquean funcionalidad core)

1. **`VentaService.crearVenta()` no mapea los detalles**
   - El método crea `Venta venta = new Venta()` pero nunca itera sobre `ventaDTO.detalles()` para crear las entidades `DetalleVenta`.
   - Luego intenta `venta.getDetalles().stream()...` lo cual retorna lista vacía.
   - **Impacto**: Las ventas se crean sin productos. El stock no se descuenta.

2. **`TiendaRepository` no extiende `JpaRepository`**
   - `public interface TiendaRepository {}` — interfaz vacía.
   - **Impacto**: No se pueden guardar ni consultar tiendas.

3. **`AuthService.register()` ignora el rol solicitado**
   - Siempre asigna `RolUsuario.VENDEDOR`.
   - **Impacto**: No se pueden crear admins ni gerentes vía API.

4. **Inconsistencia en rutas de API**
   - Productos: `/api/productos/**`
   - Ventas y Auth: `/api/v1/ventas/**`, `/api/v1/auth/**`
   - **Impacto**: Confusión en el frontend y en la configuración de seguridad.

5. **`SecurityConfig` usa roles con prefijo incorrecto**
   - `.hasRole("ADMIN")` espera `ROLE_ADMIN` pero el enum es `ADMINISTRADOR`.
   - Las authorities se configuran como `RolUsuario.ADMINISTRADOR.name()` que da `"ADMINISTRADOR"`.
   - `.hasRole("ADMIN")` busca `ROLE_ADMIN`, que no coincide con `"ADMINISTRADOR"`.
   - **Impacto**: El control de acceso por roles no funciona.

### 🟡 Altos (funcionalidad rota o insegura)

6. **`VentaService.verificarPromocionesCliente()` no persiste la promoción**
   - Crea el objeto `PromocionCliente` pero no llama a `promocionClienteRepository.save()`.

### 🟢 Medios/Bajos

7. Solo existe 1 test (`contextLoads()`). Cero cobertura de unidad o integración.
8. No hay manejo de excepciones de validación (`@Valid`).
9. `RegisterRequest` usa Lombok `@Data`/`@Builder` en vez de record como los otros DTOs.
10. El `ProductoController` en el método `ajustarInventario` recibe `@AuthenticationPrincipal Usuario usuario` pero `Usuario` es una entidad JPA con `@Data` que puede causar problemas de serialización.

---

## 3. Lo que FALTA para un MVP funcional

### 3.1 Correcciones urgentes (requeridas para que funcione)
| # | Tarea | Prioridad |
|---|---|---|
| 1 | Arreglar `VentaService.crearVenta()` — mapear detalles desde DTO | 🔴 |
| 2 | Arreglar `TiendaRepository` — extender `JpaRepository<Tienda, Long>` | 🔴 |
| 3 | Arreglar `SecurityConfig` — mapear roles correctamente | 🔴 |
| 4 | Arreglar `AuthService.register()` — respetar rol del request | 🔴 |
| 5 | Arreglar `AuthService.login()` — buscar por email/username correcto | 🟡 |
| 6 | Persistir promociones en `VentaService` | 🟡 |
| 7 | Agregar `GlobalExceptionHandler` para validación y argumentos | 🟡 |

### 3.2 Controladores y servicios faltantes

| Módulo | ¿Qué falta? |
|---|---|
| **Categorías** | `CategoriaController` + `CategoriaService`: CRUD básico |
| **Clientes** | `ClienteController` + `ClienteService`: CRUD, búsqueda por teléfono |
| **Tiendas** | `TiendaController` + `TiendaService`: CRUD, asignación de usuarios |
| **Proveedores** | `ProveedorController` + `ProveedorService`: CRUD, reporte de ventas por proveedor, cálculo de comisiones |
| **Usuarios** | `UsuarioController` + `UsuarioService`: CRUD, cambio de contraseña, activación/desactivación |
| **Inventario** | `InventarioController` + `InventarioService`: historial de movimientos, entradas y salidas |
| **Ventas** | Endpoints GET: listar ventas (paginado), ver detalle, ventas del día, cancelar venta, historial por cliente |
| **Reportes** | `ReporteController` + `ReporteService`: dashboard, ventas diarias/semanales/mensuales, productos más vendidos, corte de caja |

### 3.3 Funcionalidad POS pendiente

| Funcionalidad | Estado |
|---|---|
| Corte de caja (cierre de turno) | ❌ No implementado |
| Devoluciones / notas de crédito | ❌ No implementado |
| Múltiples métodos de pago en una venta | ❌ Solo un método por venta |
| Impresión de ticket (integración) | ❌ No implementado |
| Control de inventario en tiempo real al vender | ❌ Bug en VentaService lo impide |
| Descuentos y promociones automáticas | ⚠️ Lógica parcial, no persiste |
| Historial de precios | ❌ No implementado |
| Reportes de comisiones por proveedor | ❌ Solo query en repo, sin servicio |
| Bitácora de auditoría | ❌ No implementado |

### 3.4 Testing
- Solo 1 test de contexto (`contextLoads`).
- **Falta**: tests unitarios para servicios, tests de integración para controladores, tests de repositorios.

### 3.5 DevOps / Producción
| Item | Estado |
|---|---|
| Dockerfile | ✅ Funcional |
| docker-compose (app + PostgreSQL) | ❌ No existe |
| Variables de entorno documentadas | ⚠️ Parcial (DB, JWT) |
| Health check endpoint | ❌ No implementado |
| Logging estructurado | ❌ Sin configuración |

---

## 4. Estimación de esfuerzo restante

| Fase | Tareas | Esfuerzo estimado |
|---|---|---|
| **Corrección de bugs críticos** | 6 bugs | 1-2 días |
| **Controladores/Servicios faltantes** | 7 módulos × ~3 endpoints c/u | 4-6 días |
| **Completar Ventas** | GETs, cancelación, bug fixes | 2-3 días |
| **Reportes y dashboard** | Endpoints de estadísticas | 2-3 días |
| **Corte de caja** | Lógica + endpoints | 1-2 días |
| **Testing** | Unitarios + integración | 3-5 días |
| **DevOps** | docker-compose, health checks, logging | 1-2 días |
| **Documentación API** | Anotaciones Swagger en todos los controllers | 1 día |
| **Total estimado** | | **15-25 días hábiles** |

---

## 5. Recomendaciones

1. **Primero corregir los bugs críticos** — sin esto, el sistema no funciona ni en desarrollo.
2. **Estandarizar rutas** — usar `/api/v1/` en todos los controllers o quitarlo de todos.
3. **Completar CRUDs básicos** antes de funcionalidad avanzada (reportes, corte de caja).
4. **Agregar tests** desde el inicio para evitar regresiones al agregar funcionalidad.
5. **Crear un `docker-compose.yml`** con PostgreSQL + app para desarrollo local y testing.
6. **Los datos de prueba en V2** permiten validar flujos de venta sin depender del DataSeeder Java.

---

## 6. Conclusión

El proyecto tiene una **arquitectura bien pensada** y las **entidades correctamente modeladas** para un sistema POS de papelería. La base de seguridad (JWT + Spring Security) está implementada, y el módulo de productos está casi completo.

**Cambios recientes (22 mayo 2026)**:
- ✅ Migración de MariaDB a PostgreSQL (driver, dialect, configs)
- ✅ Flyway correctamente configurado con perfil dev (H2 modo PostgreSQL) y prod (PostgreSQL)
- ✅ Migraciones V1 (esquema completo con 12 tablas + índices + secuencia de folios) y V2 (datos de prueba: 10 productos, 2 usuarios, 3 ventas)

Sin embargo, **el núcleo del negocio (ventas) tiene bugs que impiden su funcionamiento**, y la mayoría de los módulos de soporte (categorías, clientes, tiendas, proveedores, usuarios) no tienen endpoints expuestos. Sin estos, no se puede operar el sistema.

**Para un MVP funcional mínimo desplegable en servidor, se requiere**:
- Corregir los 6 bugs críticos/altos
- Implementar CRUDs de categorías, clientes, tiendas, proveedores y usuarios
- Completar el flujo de ventas (crear, listar, cancelar)

Con esto, se tendría un sistema POS básico operable. El resto (reportes, corte de caja, devoluciones) puede ir en una segunda fase.
