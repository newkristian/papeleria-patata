# Administración de productos

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Crear, consultar y actualizar productos con categoría, proveedor, costos, precio y
existencias.

## Implementación verificada

- Existen creación, actualización y consulta por ID mediante DTOs.
- El servicio calcula el porcentaje de ganancia y valida relaciones requeridas.

## Pendientes conocidos

- No existe una operación explícita de eliminación o desactivación en el Controller.
- Falta cobertura de pruebas y revisión integral de autorización por operación.

## Criterios de aceptación

- El catálogo puede administrarse sin exponer entidades JPA como contrato HTTP.
