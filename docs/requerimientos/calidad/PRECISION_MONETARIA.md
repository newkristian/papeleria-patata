# Precisión monetaria

**Estado:** Implementado
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

## Implementación verificada

- Productos: costo, porcentaje de ganancia y precio de venta.
- Movimientos de inventario: costo unitario.
- Ventas: subtotal, descuento, impuesto y total.
- Detalles de venta: precio de lista, porcentaje, monto descontado, precio final y
  subtotal.
- Búsquedas y contratos REST directamente relacionados usan `BigDecimal`.
- Flyway V5 convierte los datos existentes redondeando a dos decimales.
- Clientes: total acumulado de compras.
- Promociones de cliente: porcentajes, compra mínima y descuento fijo.
- Proveedores: porcentaje de comisión, pagos y reportes financieros.
- Flyway V6 convierte los datos existentes de clientes y proveedores, redondeando a
  dos decimales y agregando restricciones de dominio.
- No permanecen usos de `Double` o `double` en el código Java del backend ni en sus
  pruebas.
- `VentaService` acumula compras y evalúa el umbral VIP directamente con
  `BigDecimal`, sin conversiones intermedias.

## Criterios de aceptación

- Los módulos financieros no introducen errores por representación binaria.
- El redondeo es determinista y está cubierto por pruebas de reglas financieras.
- La base de datos rechaza importes estructuralmente inválidos dentro de cada módulo
  migrado.
