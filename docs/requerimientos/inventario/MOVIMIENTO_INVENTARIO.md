# Movimientos de inventario

**Estado:** En desarrollo
**Última revisión:** 2 de septiembre de 2026

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

- Existen endpoints de entrada, salida e historial paginado/filtrado.
- Las salidas validan existencias y las operaciones son transaccionales.
- El costo se oculta a vendedores y una entrada solo incrementa el costo de catálogo
  cuando el nuevo costo es mayor.
- Los costos unitarios utilizan `BigDecimal` y `NUMERIC(19, 2)`.

## Pendientes conocidos

- Estabilizar la ejecución de las cinco pruebas unitarias existentes.
- Añadir pruebas de Controller y persistencia.
- Impedir movimientos relativos sobre cantidad desconocida.
- Evitar ajustes relativos con resultado negativo.
- Completar la fotografía de valor anterior/resultante y revisar concurrencia para
  evitar actualizaciones perdidas.
- Aplicar autorización explícita también al caso de uso y no únicamente al Controller.

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
