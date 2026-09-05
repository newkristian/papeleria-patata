# Stock mínimo y alerta de existencias

**Estado:** Implementado y verificado  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Definir el stock mínimo de cada producto y consultar los productos que requieren
reposición.

## Alcance aprobado

- El stock mínimo debe ser un entero no negativo.
- Los productos con `cantidadDesconocida = true` no aparecerán en alertas de stock
  bajo, porque todavía no existe una cantidad comparable.
- Los productos inactivos tampoco aparecerán en la alerta operativa por defecto.
- `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA` pueden modificar el stock mínimo.
- `VENDEDOR` no puede modificarlo ni utilizar una consulta administrativa que exponga
  costos.
- La modificación del umbral no altera `stockActual` ni crea un movimiento de
  inventario, porque no representa un cambio de existencias.

## Implementación verificada

- El modelo contiene stock mínimo (entero no negativo) y existe consulta dedicada
  `GET /api/v1/productos/stock-bajo`.
- La consulta excluye expresamente productos inactivos y productos con
  `cantidadDesconocida = true`.
- Modificación del stock mínimo disponible en alta y edición mediante el formulario
  de producto, sin alterar las existencias físicas ni generar movimientos de inventario.
- Alerta visual en la interfaz de catálogo (`ProductosAdminComponent` en Tarea 10)
  destacando productos con existencia inferior o igual al umbral mínimo, y filtro
  específico para consultar existencias bajas.
- Pruebas unitarias y de integración que verifican la autorización, la no negatividad
  y la exclusión de productos desconocidos.

## Cierre de pendientes del hito

- **Modificación administrativa:** Implementada en modal de productos para roles autorizados.
- **Exclusión de desconocidos e inactivos:** Validada en backend y frontend.
- **Pruebas de límites:** Verificadas en suite de `ProductoTest` y `FlujoIntegralCargaProductosIntegrationTest`.

## Criterios de aceptación

- Un valor negativo se rechaza sin modificar el producto.
- Un producto conocido con `stockActual <= stockMinimo` aparece en la alerta.
- Un producto desconocido o inactivo no aparece en la alerta operativa.
- Modificar el umbral no modifica existencias ni la bitácora de movimientos.
