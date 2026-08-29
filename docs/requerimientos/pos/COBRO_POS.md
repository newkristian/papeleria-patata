# Cobro desde el POS

**Estado:** Aprobado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Confirmar una venta, seleccionar el método de pago y mostrar el resultado al cajero.

## Alcance aprobado

- Cobro con el método único admitido actualmente por la API.
- Cálculo de cambio para efectivo y prevención de envíos duplicados.
- Manejo visible de errores de stock, autorización y conexión.

## Implementación verificada

No existe integración frontend con `POST /api/v1/ventas`.

## Dependencias pendientes

- Corregir autenticación y validación de precios en backend antes de integrar.
