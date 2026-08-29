# Estrategia de pruebas del MVP

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Proteger la lógica con impacto financiero, de inventario y autorización mediante
pruebas unitarias y de integración enfocadas en riesgo.

## Implementación verificada

- Existen cinco pruebas unitarias de Inventario y una prueba de carga del contexto.
- El frontend conserva dos pruebas base de Angular.

## Pendientes conocidos

- La prueba de contexto depende de PostgreSQL de desarrollo y no está aislada.
- Mockito requiere una configuración de agente compatible con el JDK actual.
- Una prueba frontend está desactualizada y falla al buscar el título generado.
- Faltan pruebas de ventas, autenticación, autorización, Controllers y Repositories.
