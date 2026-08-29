# Health checks de servicios

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Permitir que Docker y la operación del servidor detecten servicios disponibles.

## Implementación verificada

- PostgreSQL tiene health check en Docker Compose.

## Pendientes conocidos

- Incorporar health checks del backend y frontend.
- Definir qué dependencias participan en readiness y liveness sin exponer secretos.
