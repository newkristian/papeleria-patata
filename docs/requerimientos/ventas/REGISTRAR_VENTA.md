# Registrar venta

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Registrar atómicamente una venta, sus detalles, cliente, tienda, empleado, descuento
y afectación de inventario.

## Implementación verificada

- `POST /api/v1/ventas` crea la venta y descuenta inventario.
- Valida producto activo, stock conocido, tienda y restricciones de descuento.
- Acumula compras del cliente y genera promoción VIP al superar el umbral.

## Pendientes críticos

- El backend usa el `precioUnitario` enviado por el cliente; debe obtener el precio
  autorizado del catálogo para impedir manipulación del total.
- Añadir pruebas de atomicidad, precios, roles, descuentos, stock y promociones.
- Revisar el comportamiento de stock para productos con cantidad desconocida.

## Criterios de aceptación

- Ningún cliente HTTP puede decidir el precio final de un producto.
- Un fallo en cualquier detalle revierte completamente venta e inventario.
