# Búsqueda de productos

**Estado:** Implementado  
**Última revisión:** 2 de septiembre de 2026

## Objetivo

Localizar productos por texto, código de barras, categoría, proveedor, precio y
condición de stock.

## Implementación verificada

- `GET /api/v1/productos/buscar` combina en una sola consulta filtros opcionales de
  texto, categoría, proveedor, precio, estado y stock bajo, además de paginación.
- Sin filtro explícito de estado, todos los roles reciben únicamente productos
  activos. `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA` pueden solicitar expresamente
  activos o inactivos; `VENDEDOR` no puede eludir el filtro de activos.
- La consulta por código de barras normaliza el valor, solo encuentra productos
  activos y devuelve el DTO seguro del POS, sin costos ni datos administrativos.
- Los listados por categoría y proveedor excluyen inactivos.
- Un producto con cantidad desconocida no aparece como stock bajo.
- Hay cobertura unitaria e integral para la visibilidad por rol, el catálogo inactivo
  y la ausencia de costos en respuestas del vendedor.

## Pendientes no esenciales

- Ampliar la matriz de combinaciones de filtros, paginación y resultados vacíos cuando
  se construya la pantalla administrativa.
