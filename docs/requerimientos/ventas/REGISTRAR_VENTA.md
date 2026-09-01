# Registrar venta

**Estado:** En desarrollo  
**Última revisión:** 1 de septiembre de 2026

## Objetivo

Registrar atómicamente una venta, sus detalles, cliente, tienda, empleado,
promociones, descuentos autorizados y afectación de inventario.

## Implementación verificada

- `POST /api/v1/ventas` crea la venta y descuenta inventario.
- Valida producto activo, stock conocido, tienda y una restricción preliminar de
  acceso por rol.
- Acumula con precisión decimal exacta las compras del cliente y genera una
  promoción VIP al superar el umbral.
- El request solo identifica producto y cantidad; el precio se obtiene del catálogo
  persistido.
- Los productos repetidos en el request se consolidan por `productoId` (sumando
  cantidades) antes de evaluar stock y promociones, para no evadir ni duplicar el
  beneficio de un escalón de cantidad.
- Cada línea consolidada se evalúa contra el motor de promociones automáticas (por
  cantidad de producto/categoría y por cliente, incluida VIP) y aplica como máximo una
  promoción ganadora; el resultado se recalcula íntegramente en el backend.
- Los importes de Producto, Venta, DetalleVenta y el acumulado del cliente utilizan
  `BigDecimal` y columnas `NUMERIC` con dos decimales.
- Cada detalle conserva precio de lista, tipo y monto de descuento, precio final,
  referencia a la promoción aplicada (de producto o de cliente, excluyentes), subtotal,
  autorizador y motivo. Los campos de autorización manual permanecen sin usar hasta
  implementar la Tarea 6.
- Un fallo en cualquier línea (producto inactivo, precio inválido o stock insuficiente)
  revierte la venta completa antes de persistir nada; verificado con prueba manual de
  atomicidad.
- La venta histórica conserva su fotografía de descuento aunque la promoción usada se
  desactive o modifique después.

## Pendientes conocidos

- Implementar autorizaciones manuales de un solo uso (Tarea 6).
- Añadir pruebas de integración de extremo a extremo (además de las unitarias ya
  existentes) y de aislamiento por tienda.
- Revisar el comportamiento de stock para productos con cantidad desconocida.
- Defecto preexistente detectado (no corregido, fuera de alcance): los `INSERT` de
  `ventas` en `V2__datos_prueba.sql` usan IDs explícitos sin resincronizar
  `ventas_id_seq`, por lo que la primera venta en una base recién sembrada falla con
  violación de llave primaria hasta ejecutar `setval` manualmente.

## Criterios de aceptación

- Ningún cliente HTTP puede decidir el precio final de un producto.
- Ningún cliente HTTP puede decidir la promoción o el descuento efectivo.
- Las líneas deben conservar precio de lista, promoción, descuento y total como
  fotografía histórica.
- Un fallo en cualquier detalle revierte completamente venta e inventario.

## Requerimientos relacionados

- `PROMOCIONES_PRODUCTO.md`
- `DESCUENTO_POR_CANTIDAD.md`
- `AUTORIZACION_DESCUENTO_MANUAL.md`
- `../calidad/PRECISION_MONETARIA.md`
