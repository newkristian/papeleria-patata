# Registrar venta

**Estado:** Implementado
**Última revisión:** 2 de septiembre de 2026

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
  referencia a la promoción aplicada (de producto o de cliente, excluyentes) o a la
  autorización manual consumida, subtotal, autorizador y motivo.
- Una línea puede traer una referencia opaca de autorización de descuento manual
  (Tarea 6, `POST /api/v1/autorizaciones-descuento`); si la trae, `crearVenta` la
  consume y revalida dentro de la misma transacción, y ese descuento reemplaza (no se
  acumula con) la promoción automática de esa línea. El contrato completo para el
  frontend (campos `carritoId`/`autorizacionDescuento`, ejemplos de request/response y
  manejo de errores) está en `AUTORIZACION_DESCUENTO_MANUAL.md`, sección "Contrato
  para el frontend".
- Un fallo en cualquier línea (producto inactivo, precio inválido, stock insuficiente
  o autorización manual inválida/expirada/reutilizada) revierte la venta completa
  antes de persistir nada; verificado con prueba unitaria y con una prueba de
  integración real contra PostgreSQL (T8) que confirma que el stock ya descontado de
  una línea previa también se revierte.
- La venta histórica conserva su fotografía de descuento aunque la promoción usada se
  desactive o modifique después; verificado también con integración real, no solo
  con mocks.
- Control de acceso horizontal por tienda (T8): `GET /ventas`, `GET /ventas/{id}` y
  `GET /ventas/cliente/{clienteId}` filtran por la tienda del usuario autenticado
  para `VENDEDOR` (`ADMINISTRADOR` ve todas). Una venta de otra tienda responde 404,
  no 403, para no confirmar que el ID existe en otra tienda.
- Interfaz de usuario completa en el POS (Tarea 7 / `docs/trabajo-completado/TAREAS_POS.md`):
  buscador, carrito, cobro con presentación de promociones por línea, botón de
  descuento manual y modal de reautenticación.

## Pendientes conocidos

- Revisar el comportamiento de stock para productos con cantidad desconocida.
- Pruebas de componente del frontend (`CobroComponent`,
  `ModalAutorizacionDescuentoComponent`): no se agregaron en T8, siguiendo la
  convención ya establecida en este frontend de no tener specs por componente (solo a
  nivel de servicio); es una decisión consciente, no un olvido — revisar si conviene
  cambiar esa convención más adelante.

## Criterios de aceptación

- Ningún cliente HTTP puede decidir el precio final de un producto.
- Ningún cliente HTTP puede decidir la promoción o el descuento efectivo.
- Las líneas deben conservar precio de lista, promoción, descuento y total como
  fotografía histórica.
- Un fallo en cualquier detalle revierte completamente venta e inventario.
- Un `VENDEDOR` no puede ver ni consultar ventas de otra tienda.

## Requerimientos relacionados

- `PROMOCIONES_PRODUCTO.md`
- `DESCUENTO_POR_CANTIDAD.md`
- `AUTORIZACION_DESCUENTO_MANUAL.md`
- `../calidad/PRECISION_MONETARIA.md`
