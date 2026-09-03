# Inventario por tienda y transferencias

**Estado:** Aprobado, diferido
**Última revisión:** 2 de septiembre de 2026

## Objetivo

Mantener un catálogo global de productos y existencias independientes por tienda,
permitiendo transferir unidades de una tienda a otra con trazabilidad completa.

## Alcance aprobado para implementación futura

- Los productos, categorías, proveedores, precios y fotografías pertenecen al catálogo
  global y pueden ser consultados por todas las tiendas.
- Cada tienda mantiene su propia existencia y stock mínimo por producto.
- Una venta descuenta exclusivamente las existencias de la tienda del usuario que la
  registra.
- Entradas, salidas, conteos y ajustes afectan una tienda explícita y autorizada.
- Una transferencia registra tienda origen, tienda destino, producto, cantidad,
  usuario responsable, motivo, fecha y estado.
- La salida de origen y la entrada en destino deben ser atómicas al completar una
  transferencia.
- La tienda origen debe tener existencias conocidas y suficientes. Ninguna operación
  puede producir stock negativo.
- Debe definirse el tratamiento de productos con cantidad desconocida en origen o
  destino antes de implementar transferencias.
- La autorización debe impedir que un usuario opere inventario de tiendas fuera de su
  alcance.

## Estado actual y decisión transitoria

- El modelo actual almacena `stockActual`, `stockMinimo` y `cantidadDesconocida`
  directamente en `Producto`; por tanto, todas las tiendas comparten una sola
  existencia.
- El alcance de `docs/trabajo-actual/TAREAS_CARGA_PRODUCTOS.md` mantendrá
  temporalmente este modelo global compartido.
- Esta decisión transitoria permite completar el mantenimiento de productos sin
  introducir ahora una migración transversal en ventas, inventario, búsquedas y
  autorización.

## Impacto técnico futuro

- Crear una relación de inventario por `producto_id` y `tienda_id`, con unicidad para
  el par y controles de integridad.
- Migrar el stock global existente a una estrategia de distribución que deberá ser
  aprobada antes de ejecutar la migración.
- Retirar del producto los atributos de existencia que pasen a pertenecer a la tienda,
  mediante migraciones compatibles con datos reales.
- Adaptar ventas, movimientos, conteos, stock bajo, búsquedas del POS, autorizaciones
  de descuento y contratos REST.
- Incorporar el modelo transaccional y la bitácora de transferencias.
- Adaptar el frontend para seleccionar y visualizar la tienda correcta sin permitir
  manipulación de alcance desde el cliente.
- Añadir pruebas de concurrencia, autorización, atomicidad y aislamiento entre
  tiendas.

## Decisiones pendientes antes de implementar

- Estrategia para distribuir el stock global existente entre tiendas durante la
  migración.
- Roles autorizados para solicitar, enviar, recibir, cancelar y consultar
  transferencias.
- Flujo de transferencia: inmediata o con estados de solicitud, tránsito, recepción y
  cancelación.
- Tratamiento de diferencias entre cantidad enviada y recibida.
- Comportamiento cuando la cantidad sea desconocida en origen o destino.

## Criterios de aceptación futuros

- Dos tiendas pueden consultar el mismo producto con cantidades independientes.
- Una venta modifica únicamente el inventario de su tienda.
- Una transferencia completada descuenta origen y aumenta destino exactamente una
  vez.
- Un fallo revierte ambos lados de la transferencia.
- Un usuario no puede consultar ni modificar inventario de una tienda no autorizada.
- El historial permite reconstruir entradas, salidas y transferencias por producto y
  tienda.

