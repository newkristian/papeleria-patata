# Registrar venta

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Registrar atómicamente una venta, sus detalles, cliente, tienda, empleado,
promociones, descuentos autorizados y afectación de inventario.

## Implementación verificada

- `POST /api/v1/ventas` crea la venta y descuenta inventario.
- Valida producto activo, stock conocido, tienda y una restricción preliminar de
  acceso por rol.
- Acumula compras del cliente y genera promoción VIP al superar el umbral.
- El request solo identifica producto y cantidad; el precio se obtiene del catálogo
  persistido.
- Las ventas se registran temporalmente sin descuento hasta implementar el motor de
  promociones y la autorización manual.
- Los importes de Producto, Venta y DetalleVenta utilizan `BigDecimal` y columnas
  `NUMERIC` con dos decimales.
- Cada detalle conserva precio de lista, tipo y monto de descuento, precio final,
  autorizador, motivo y subtotal. Los campos de descuento se inicializan como
  `NINGUNO` y cero durante esta etapa.

## Pendientes conocidos

- Implementar promociones calculadas por producto y autorizaciones manuales de un
  solo uso.
- Migrar `Cliente.totalCompras` a `BigDecimal` en la Tarea 2B y retirar la conversión
  temporal utilizada al acumular compras.
- Añadir pruebas de atomicidad, precios, roles, descuentos, stock y promociones.
- Revisar el comportamiento de stock para productos con cantidad desconocida.

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
