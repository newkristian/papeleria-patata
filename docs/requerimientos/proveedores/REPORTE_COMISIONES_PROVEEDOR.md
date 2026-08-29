# Reporte de ventas y comisiones por proveedor

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Calcular ventas atribuibles a cada proveedor y la comisión correspondiente dentro de
un periodo definido.

## Implementación verificada

- Existe una consulta de ventas agrupadas por producto y proveedor.
- Existen DTOs preliminares de reporte y porcentaje de comisión en el proveedor.
- Los montos del reporte, pagos y comisiones usan `BigDecimal`; Flyway V6 los
  persiste como `NUMERIC` con dos decimales y restricciones no negativas.

## Pendientes conocidos

- Implementar el caso de uso y endpoint con rango de fechas.
- Definir tratamiento de ventas canceladas, devoluciones, impuestos y redondeos.
- Añadir autorización y pruebas financieras.
