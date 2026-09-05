# Administración de categorías

**Estado:** Implementado y verificado  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Administrar las categorías que organizan el catálogo de productos.

## Dependencia para el alta de productos

- Todo producto requiere una categoría existente; `productos.categoria_id` es una
  clave foránea `NOT NULL` y `Producto.categoria` también es obligatoria en JPA.
- Antes de habilitar el formulario frontend de productos debe existir un flujo usable
  para listar y crear categorías.
- `ADMINISTRADOR` e `INVENTARISTA` pueden crear y editar categorías. Ambos roles deben
  poder completar el recorrido iniciar sesión → crear o seleccionar categoría → crear
  producto sin preparación manual de base de datos.
- La categoría seleccionada siempre se validará nuevamente en backend.

## Implementación verificada

- CRUD REST completo con contratos Request/Response.
- Escritura restringida por roles y consultas optimizadas para evitar N+1.
- Los campos se normalizan, los nombres duplicados sin distinción de mayúsculas se
  rechazan con 409 y un recurso inexistente devuelve 404.
- El borrado comprueba tanto productos como promociones relacionados y responde 409
  con un mensaje controlado, sin filtrar errores de integridad internos.
- Pruebas unitarias y de API verifican las relaciones y los permisos; administrador e
  inventarista pueden crear una categoría y usar su ID inmediatamente para crear un
  producto.
- Mantenimiento frontend completo en Angular (`CategoriasAdminComponent`, `CategoriaService`,
  ruta `/admin/categorias`), con listado en tarjetas, modal de creación y edición,
  validaciones reactivas y actualización inmediata del selector de categorías en el
  formulario de productos.

## Cierre de pendientes del hito

- **Mantenimiento frontend de categorías:** Completado en Tarea 9 y verificado en suite Vitest.

## Pendientes no esenciales

- Definir en el futuro si el catálogo de categorías requiere estado activo y borrado
  lógico, según la dependencia descrita más abajo.

## Dependencia no bloqueante

- Debe definirse si las categorías necesitarán borrado lógico y estado activo. El
  backend actual permite borrado físico solo cuando no existen productos asociados y
  rechaza el borrado en caso contrario. Esta decisión no impide crear productos, pero
  deberá resolverse antes de ampliar el ciclo de vida administrativo de categorías.

## Criterios de aceptación del flujo dependiente

- Un administrador o inventarista puede listar categorías y crear una nueva desde la
  interfaz administrativa.
- La categoría recién creada puede seleccionarse inmediatamente al dar de alta un
  producto.
- Un rol no autorizado no puede crear, editar ni eliminar categorías mediante una
  solicitud HTTP directa.
