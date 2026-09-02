# Inventario con cantidad desconocida

**Estado:** En desarrollo  
**Última revisión:** 2 de septiembre de 2026

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
- La creación de ventas omite la validación de existencias para estos productos.

## Pendientes conocidos

- Evitar que ventas y salidas descuenten el valor provisional de `stockActual`.
- Rechazar movimientos relativos hasta completar el conteo absoluto.
- Verificar atomicidad y concurrencia entre el conteo y una venta simultánea.
- Excluir estos productos de consultas de stock bajo.
- Añadir pruebas de transición y regresión en ventas.

## Criterios de aceptación

- Vender repetidamente un producto desconocido deja `stockActual` sin cambios.
- Una entrada, salida o ajuste relativo desconocido se rechaza con un error de negocio
  comprensible.
- Un conteo absoluto de cero o mayor fija la existencia y desactiva la bandera.
- La primera venta posterior al conteo utiliza inmediatamente el control normal de
  stock.
- Dos operaciones concurrentes no pueden perder actualizaciones ni aceptar una salida
  indebida.
