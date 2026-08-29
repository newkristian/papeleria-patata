# Movimientos de inventario

**Estado:** Implementado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Registrar entradas y salidas manuales y consultar la bitácora histórica con filtros.

## Implementación verificada

- Existen endpoints de entrada, salida e historial paginado/filtrado.
- Las salidas validan existencias y las operaciones son transaccionales.
- El costo se oculta a vendedores y una entrada solo incrementa el costo de catálogo
  cuando el nuevo costo es mayor.
- Los costos unitarios utilizan `BigDecimal` y `NUMERIC(19, 2)`.

## Pendientes no esenciales

- Estabilizar la ejecución de las cinco pruebas unitarias existentes.
- Añadir pruebas de Controller y persistencia.
