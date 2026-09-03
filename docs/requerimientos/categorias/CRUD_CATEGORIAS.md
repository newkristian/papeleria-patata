# Administración de categorías

**Estado:** En desarrollo
**Última revisión:** 2 de septiembre de 2026

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

## Pendientes no esenciales

- Añadir pruebas unitarias y de API.

## Pendientes para el mantenimiento de productos

- Crear modelos, servicio HTTP y mantenimiento frontend de categorías antes del
  formulario de productos.
- Manejar de forma consistente nombres duplicados y la eliminación de una categoría
  relacionada con productos.
- Considerar también promociones relacionadas por `promociones.categoria_id`; el
  servicio actual solo cuenta productos antes del borrado y una promoción asociada
  podría terminar en una violación de clave foránea no controlada.
- Añadir pruebas de autorización para `ADMINISTRADOR`, `INVENTARISTA` y roles no
  permitidos.

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
