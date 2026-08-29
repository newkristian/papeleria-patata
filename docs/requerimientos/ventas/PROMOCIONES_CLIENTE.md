# Promociones por nivel de cliente

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

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

## Pendientes conocidos

- Integrar la promoción VIP como candidata del motor descrito en
  `PROMOCIONES_PRODUCTO.md`.
- Comparar su beneficio con otras promociones y aplicar solamente la opción más
  favorable; no debe acumularse.
- Definir efecto de cancelaciones y devoluciones sobre nivel y promociones.
- Añadir pruebas de umbral, vigencia, duplicados y concurrencia.
