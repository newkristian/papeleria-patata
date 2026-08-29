# Renovación de token de acceso

**Estado:** En desarrollo
**Última revisión:** 28 de agosto de 2026

## Objetivo

Renovar un token de acceso usando un refresh token válido sin solicitar nuevamente
las credenciales del empleado.

## Implementación verificada

- El backend expone `POST /api/v1/auth/refresh-token` y valida el refresh token.
- El frontend conserva el refresh token en `localStorage` al iniciar sesión y lo
  elimina al cerrar sesión.

## Pendientes conocidos

- El frontend todavía no solicita automáticamente la renovación del access token.
- Falta definir rotación, expiración de sesión y reacción ante refresh inválido.
- Atender el riesgo XSS documentado en `seguridad/PROTECCION_XSS.md`.

## Criterios de aceptación

- Un refresh token válido produce un access token nuevo.
- Un refresh token inválido o expirado es rechazado sin revelar datos sensibles.
