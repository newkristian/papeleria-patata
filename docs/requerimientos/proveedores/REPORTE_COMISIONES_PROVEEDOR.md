# Reporte de ventas y comisiones por proveedor

**Estado:** Aprobado, diferido (módulo de reportes)  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Calcular ventas atribuibles a cada proveedor y la comisión correspondiente dentro de
un periodo definido.

## Implementación verificada

- Existe una consulta de ventas agrupadas por producto y proveedor.
- Existen DTOs preliminares de reporte y porcentaje de comisión en el proveedor.
- Los montos del reporte, pagos y comisiones usan `BigDecimal`; Flyway V6 los
  persiste como `NUMERIC` con dos decimales y restricciones no negativas.
- El catálogo de proveedores ya incluye `porcentajeComision` (0 a 100 con 2 decimales),
  estado activo y protección de proveedor de sistema `PENDIENTE` (Flyway V13).

## Pendientes conocidos

- Implementar el caso de uso y endpoint con rango de fechas en el módulo correspondiente.
- Definir tratamiento de ventas canceladas, devoluciones, impuestos y redondeos.
- Definir si el reporte histórico agrupa por el proveedor actual del producto o por
  una fotografía del proveedor al momento de la venta.
- Añadir autorización y pruebas financieras.
