# Promociones por nivel de cliente

**Estado:** En desarrollo  
**Última revisión:** 1 de septiembre de 2026

## Objetivo

Acumular compras de clientes registrados y asignar promociones persistentes cuando
cumplan las reglas definidas.

## Implementación verificada

- Una venta registrada acumula el total de compras del cliente.
- Al superar $5,000, un cliente Regular pasa a VIP y recibe una promoción de 10%
  con vigencia de seis meses.
- El total acumulado, los porcentajes y los importes de promociones se calculan y
  persisten con precisión decimal exacta mediante `BigDecimal` y `NUMERIC`.
- Existe una prueba de venta que cruza el umbral VIP y verifica tanto el acumulado
  exacto como el porcentaje de la promoción creada.
- `PromocionCliente` tiene columna `prioridad` y vigencia obligatoria
  (`fechaInicio`/`fechaFin`, ambas inclusivas). Participa en `MotorPromocionesService`
  como una candidata más, comparada por beneficio, prioridad, vigencia definida e ID,
  sin acumularse con otras promociones.
- `montoDescuentoFijo` (descuento de monto fijo) se evalúa como candidata completa en
  el mismo grupo, capado al subtotal de la línea, con porcentaje efectivo calculado
  para fines de auditoría.
- Una venta anónima nunca evalúa promociones de cliente.

## Pendientes conocidos

- Definir efecto de cancelaciones y devoluciones sobre nivel y promociones.
- Añadir pruebas de concurrencia sobre la creación de la promoción VIP.
