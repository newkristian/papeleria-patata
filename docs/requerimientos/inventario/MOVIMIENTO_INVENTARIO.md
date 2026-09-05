# Movimientos de inventario

**Estado:** Implementado y verificado  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Registrar entradas y salidas manuales y consultar la bitácora histórica con filtros.

## Alcance aprobado

- `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA` pueden registrar entradas, salidas y
  ajustes; `VENDEDOR` no puede hacerlo.
- Los mismos tres roles pueden consultar la bitácora completa y sus costos.
- Una entrada o salida requiere producto activo, cantidad positiva y motivo
  obligatorio con longitud acotada.
- Una salida de inventario conocido debe validar existencias y nunca producir un
  valor negativo.
- Un ajuste relativo de inventario conocido puede sumar o restar, pero se rechazará si
  el resultado es negativo.
- Los productos con cantidad desconocida solo admiten el conteo absoluto definido en
  `CANTIDAD_DESCONOCIDA.md`.
- La bitácora deberá distinguir movimientos relativos de conteos absolutos y conservar
  la información suficiente para conocer el valor anterior y resultante.
- Las modificaciones y su movimiento se guardarán en la misma transacción.

## Implementación verificada

- Existen endpoints de entrada, salida, ajuste e historial paginado/filtrado.
- Las salidas validan existencias y las operaciones son transaccionales con bloqueo
  pesimista para concurrencia.
- El costo se oculta a vendedores (`costoUnitario = null`) tanto en backend como
  en la interfaz frontend.
- Una entrada solo incrementa el costo de catálogo cuando el nuevo costo es mayor.
- Los costos unitarios utilizan `BigDecimal` y `NUMERIC(19, 2)`.
- Movimientos relativos y salidas sobre productos con cantidad desconocida son
  rechazados inmediatamente.
- Consulta dinámica con `JpaSpecificationExecutor` en `InventarioMovimientoRepository`,
  resolviendo parámetros nulos de forma compatible con PostgreSQL 16.
- Pantalla de inventario completa en frontend (`InventarioAdminComponent`, `InventarioService`,
  ruta `/admin/inventario`) con modales para entrada, salida y ajuste, y tabla paginada.

## Cierre de pendientes del hito

- **Estabilidad de pruebas y cobertura:** 7 pruebas unitarias de `InventarioServiceTest`
  y suites de integración E2E pasando exitosamente.
- **Validación de cantidad desconocida y no negatividad:** Implementadas y verificadas.
- **Autorización en controller y servicio:** Aplicada en backend para los 4 roles.
- **Interfaz administrativa:** Completada en Tarea 11.

## Criterios de aceptación

- Un rol no permitido recibe 403 al intentar modificar inventario.
- Una operación inválida no modifica ni el producto ni la bitácora.
- Una operación válida actualiza producto y movimiento atómicamente.
- La consulta paginada permite auditar quién, cuándo, por qué y cómo cambió el stock.

## Evolución futura

- El catálogo seguirá siendo global, pero las existencias y movimientos pertenecerán
  a cada tienda y se permitirán transferencias entre ellas. El alcance y las
  decisiones pendientes se encuentran en `STOCK_POR_TIENDA.md`.
- Hasta implementar esa evolución, todas las tiendas utilizarán temporalmente la misma
  existencia global almacenada en `Producto`.
