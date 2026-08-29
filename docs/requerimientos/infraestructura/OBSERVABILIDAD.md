# Logging y observabilidad

**Estado:** En análisis  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Diagnosticar fallos de producción mediante logs consistentes y señales operativas.

## Implementación verificada

- Algunos servicios y el manejador global generan logs estructurados por operación.

## Pendientes conocidos

- Homologar formato, niveles, correlación y política de retención.
- Evitar datos personales, tokens, contraseñas y secretos en logs.
- Definir métricas y alertas mínimas del MVP.
