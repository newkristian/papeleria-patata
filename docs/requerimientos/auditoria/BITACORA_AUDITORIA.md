# Bitácora de auditoría

**Estado:** Borrador  
**Última revisión:** 28 de agosto de 2026

## Objetivo preliminar

Registrar acciones sensibles con actor, fecha, operación y recurso afectado sin
almacenar secretos ni datos innecesarios.

## Alcance por definir

- Operaciones auditables: usuarios, inventario, precios, ventas y caja.
- Consulta por administradores, integridad, retención y anonimización.

## Implementación verificada

Los movimientos de inventario tienen bitácora propia, pero no existe auditoría
general del sistema.

Como parte de T8 (`docs/trabajo-completado/TAREAS_VENTAS.md`) se completó, de forma
acotada al flujo de descuentos y promociones, la fotografía de auditoría que exige
`ventas/AUTORIZACION_DESCUENTO_MANUAL.md`: `autorizaciones_descuento` conserva
vendedor, autorizador, rol del autorizador en el momento de emitir, tienda, producto,
cantidad, costo considerado, promoción automática disponible, porcentaje/monto
aplicado, motivo y fecha; nunca contraseñas ni el token en claro. Esto no sustituye
una bitácora general del sistema (usuarios, inventario, precios, caja) — ese alcance
sigue sin definirse, como dice este documento.
