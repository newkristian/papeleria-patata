# Stock mínimo y alerta de existencias

**Estado:** En desarrollo  
**Última revisión:** 2 de septiembre de 2026

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

- El modelo contiene stock mínimo y existe consulta de productos con stock bajo.
- Existe ajuste manual de inventario sobre productos.

## Pendientes conocidos

- Documentar y verificar el flujo administrativo para modificar el stock mínimo.
- Añadir pruebas de límites y productos con cantidad desconocida.
- Corregir la consulta para excluir cantidades desconocidas e inactivos.
- Aplicar autorización explícita y añadir la interfaz administrativa.

## Criterios de aceptación

- Un valor negativo se rechaza sin modificar el producto.
- Un producto conocido con `stockActual <= stockMinimo` aparece en la alerta.
- Un producto desconocido o inactivo no aparece en la alerta operativa.
- Modificar el umbral no modifica existencias ni la bitácora de movimientos.
