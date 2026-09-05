# Inventario con cantidad desconocida

**Estado:** Implementado y verificado  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Permitir vender productos cuyo inventario inicial todavía no ha sido contabilizado,
sin bloquear la operación por stock insuficiente ni persistir cantidades ficticias
negativas.

## Regla de negocio aprobada

- Mientras `cantidadDesconocida = true`, las ventas no validan ni modifican
  `stockActual`.
- No se admitirán entradas, salidas ni ajustes relativos mientras la cantidad sea
  desconocida, porque no existe una base confiable sobre la cual aplicarlos.
- El conteo se completa por producto mediante un ajuste absoluto no negativo.
- El ajuste absoluto fija `stockActual` y cambia `cantidadDesconocida` a `false` dentro
  de la misma transacción.
- Una vez conocida la cantidad, ventas y movimientos vuelven a validar y modificar
  existencias normalmente y nunca podrán llevarlas por debajo de cero.
- Un producto con cantidad desconocida no se considera agotado ni con stock bajo.
- El conteo gradual se realiza producto por producto y no requiere detener la venta de
  los demás artículos de la tienda.

## Implementación verificada

- Flyway, entidad y DTOs contienen la bandera `cantidadDesconocida`.
- La creación de ventas omite la validación de existencias para estos productos y
  no altera `stockActual` mientras sea desconocido.
- Movimientos relativos (entradas, salidas, ajustes relativos) son rechazados
  con error de negocio descriptivo mientras la cantidad sea desconocida.
- El ajuste absoluto (`AjusteInventarioDTO` con `esFijarStockAbsoluto = true`) fija
  la existencia real y apaga atómicamente `cantidadDesconocida` bajo `@Lock(PESSIMISTIC_WRITE)`.
- Consulta de stock bajo excluye automáticamente productos con `cantidadDesconocida = true`.
- Interfaz en frontend (`InventarioAdminComponent` en Tarea 11): tarjeta destacada
  de productos "Por contar" con botón de acción inmediata "Conteo inicial" que abre
  el modal en modo absoluto para fijar existencias y pasar a control normal.
- Pruebas unitarias y de integración (`SemanticaInventarioIntegrationTest` y
  `FlujoIntegralCargaProductosIntegrationTest`) verificando el ciclo completo.

## Cierre de pendientes del hito

- **Evitar descuento provisional en ventas:** Implementado y verificado en Tarea 5.
- **Rechazo de movimientos relativos:** Implementado y verificado en Tarea 5.
- **Atomicidad y concurrencia pesimista:** Implementado y verificado en Tareas 5 y 12.
- **Exclusión de stock bajo:** Implementado y verificado en Tareas 4 y 10.
- **Flujo frontend de conteo inicial:** Implementado y verificado en Tarea 11.

## Criterios de aceptación

- Vender repetidamente un producto desconocido deja `stockActual` sin cambios.
- Una entrada, salida o ajuste relativo desconocido se rechaza con un error de negocio
  comprensible.
- Un conteo absoluto de cero o mayor fija la existencia y desactiva la bandera.
- La primera venta posterior al conteo utiliza inmediatamente el control normal de
  stock.
- Dos operaciones concurrentes no pueden perder actualizaciones ni aceptar una salida
  indebida.
