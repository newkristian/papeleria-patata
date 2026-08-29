# Inventario con cantidad desconocida

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Permitir vender productos cuyo inventario inicial todavía no ha sido contabilizado,
sin bloquear la operación por stock insuficiente.

## Implementación verificada

- Flyway, entidad y DTOs contienen la bandera `cantidadDesconocida`.
- La creación de ventas omite la validación de existencias para estos productos.

## Pendientes conocidos

- Revisar que una venta no produzca existencias negativas mientras la cantidad siga
  marcada como desconocida.
- Verificar el flujo que fija el stock real y desactiva la bandera.
