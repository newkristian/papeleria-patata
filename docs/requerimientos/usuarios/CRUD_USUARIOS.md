# Administración de usuarios

**Estado:** Implementado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Crear, consultar, actualizar, activar, desactivar y administrar contraseñas de
usuarios del sistema.

## Implementación verificada

- Existen Controller, Service, Repository, Mapper y DTOs separados.
- Las contraseñas se almacenan con BCrypt.
- El alta fuerza cambio de contraseña y las operaciones administrativas tienen
  controles de autorización.

## Pendientes no esenciales

- Añadir pruebas de autorización, validaciones y casos de conflicto.
