# Cambio obligatorio de contraseña

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Obligar a los usuarios con contraseña temporal a establecer una contraseña propia.

## Implementación verificada

- La entidad y la base de datos incluyen `requiereCambioPassword`.
- La respuesta de autenticación expone el indicador.
- El backend dispone de operaciones de cambio y restablecimiento de contraseña.

## Pendientes conocidos

- El frontend ignora el indicador y no bloquea el acceso hasta completar el cambio.

## Criterios de aceptación

- Un usuario marcado debe cambiar su contraseña antes de acceder a funciones del POS.
