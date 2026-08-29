# Inicio de sesión con JWT

**Estado:** Implementado
**Última revisión:** 28 de agosto de 2026

## Objetivo

Autenticar empleados y entregar tokens firmados para consumir la API protegida.

## Implementación verificada

- El backend expone `POST /api/v1/auth/login` y genera `accessToken` y `refreshToken`.
- El frontend incluye formulario reactivo, interceptor JWT y guard de ruta.
- El frontend guarda `accessToken` en `sessionStorage` y `refreshToken` en
  `localStorage` usando los nombres reales del contrato.
- El cierre de sesión elimina ambos tokens.

## Consideraciones posteriores

- Definir tratamiento de expiración, respuestas 401 y cierre de sesión automático.
- Atender el riesgo XSS documentado en `seguridad/PROTECCION_XSS.md`.

## Criterios de aceptación

- Credenciales válidas permiten entrar al POS y consumir un endpoint protegido.
- Credenciales inválidas no almacenan tokens y muestran un error comprensible.
