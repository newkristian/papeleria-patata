# Cancelar venta

**Estado:** Borrador  
**Última revisión:** 28 de agosto de 2026

## Objetivo preliminar

Anular una venta sin eliminar su registro y restituir correctamente el inventario.

## Alcance por definir

- Roles autorizados, límite de tiempo y motivo obligatorio.
- Restitución de stock, compras acumuladas y promociones.
- Tratamiento de pagos, corte de caja y auditoría.
- Idempotencia y prohibición de cancelar dos veces.

## Implementación verificada

Existe el estado `CANCELADA`, pero no hay caso de uso ni endpoint de cancelación.
