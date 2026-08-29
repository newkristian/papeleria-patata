# Precisión monetaria

**Estado:** En desarrollo
**Última revisión:** 28 de agosto de 2026

## Objetivo

Realizar cálculos y persistir importes financieros con precisión decimal exacta y
reglas de redondeo explícitas.

## Convención aprobada

- Java: `BigDecimal`.
- PostgreSQL para importes: `NUMERIC(19, 2)`.
- PostgreSQL para porcentajes: `NUMERIC(5, 2)`.
- Escala monetaria: 2 decimales.
- Redondeo comercial: `RoundingMode.HALF_UP`.
- Los valores decimales deben construirse desde texto o mediante operaciones exactas,
  nunca desde literales binarios `double`.

## Implementación verificada — Tarea 2A

- Productos: costo, porcentaje de ganancia y precio de venta.
- Movimientos de inventario: costo unitario.
- Ventas: subtotal, descuento, impuesto y total.
- Detalles de venta: precio de lista, porcentaje, monto descontado, precio final y
  subtotal.
- Búsquedas y contratos REST directamente relacionados usan `BigDecimal`.
- Flyway V5 convierte los datos existentes redondeando a dos decimales.

## Pendiente — Tarea 2B

- Total acumulado de compras del cliente.
- Promociones de cliente.
- Porcentajes y montos de proveedores y comisiones.
- Reportes financieros relacionados.

Mientras `Cliente.totalCompras` continúe como `Double`, `VentaService` realiza una
conversión temporal y explícita al actualizarlo. Este puente debe eliminarse en 2B
antes de integrar promociones VIP.

## Criterios de aceptación

- Los módulos financieros no introducen errores por representación binaria.
- El redondeo es determinista y está cubierto por pruebas de reglas financieras.
- La base de datos rechaza importes estructuralmente inválidos dentro de cada módulo
  ya migrado.
