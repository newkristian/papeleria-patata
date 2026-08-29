# Ejecución con Docker Compose

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Ejecutar PostgreSQL, backend y frontend Nginx como un stack reproducible.

## Implementación verificada

- Existe un `docker-compose.yml` válido con los tres servicios y volúmenes.
- Existen Dockerfiles multi-etapa y proxy `/api/` en Nginx.

## Pendientes críticos

- Eliminar valores inseguros por defecto para contraseñas y JWT en producción.
- Probar construcción, migraciones, persistencia de uploads y flujo HTTP completo.
- Documentar variables y perfiles por ambiente.
