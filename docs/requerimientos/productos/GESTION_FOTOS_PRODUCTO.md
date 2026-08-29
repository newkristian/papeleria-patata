# Gestión de fotografías de producto

**Estado:** Implementado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Subir, consultar, descargar, eliminar y seleccionar la fotografía principal de un
producto, incluyendo miniaturas.

## Implementación verificada

- Existen endpoints multipart y un servicio de almacenamiento en filesystem.
- Se generan y persisten referencias a miniaturas y foto principal.

## Pendientes no esenciales

- Añadir pruebas de tamaño, tipo de archivo, acceso y eliminación física.
