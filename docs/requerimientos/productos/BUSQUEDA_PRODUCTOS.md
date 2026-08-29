# Búsqueda de productos

**Estado:** Implementado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Localizar productos por texto, código de barras, categoría, proveedor, precio y
condición de stock.

## Implementación verificada

- `GET /api/v1/productos/buscar` ofrece filtros y paginación.
- Existen consultas por código de barras, categoría, proveedor y stock bajo.

## Pendientes no esenciales

- Añadir pruebas de combinaciones de filtros, paginación y resultados vacíos.
