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

## Pendientes conocidos

- Verificar si la promoción debe aplicarse automáticamente al registrar ventas.
- Definir efecto de cancelaciones y devoluciones sobre nivel y promociones.
- Añadir pruebas de umbral, vigencia, duplicados y concurrencia.
