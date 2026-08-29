# Protección contra Cross-Site Scripting (XSS)

**Estado:** En análisis
**Última revisión:** 28 de agosto de 2026

## Objetivo

Reducir el riesgo de que código JavaScript inyectado acceda a credenciales de sesión,
datos sensibles o acciones autenticadas del POS.

## Riesgo conocido

El `accessToken` se conserva en `sessionStorage` y el `refreshToken` en
`localStorage`. Ambos almacenes son accesibles desde JavaScript ejecutado en el mismo
origen. Una vulnerabilidad XSS podría extraerlos; el refresh token tiene mayor impacto
porque su vigencia permite solicitar nuevos access tokens.

## Controles actuales

- Angular escapa interpolaciones de texto de forma predeterminada.
- No se ha identificado uso de renderizado arbitrario mediante `innerHTML`.
- El access token deja de persistir cuando termina la sesión del navegador.

## Mitigaciones pendientes

- Evaluar la migración del refresh token a una cookie `HttpOnly`, `Secure` y con una
  política `SameSite` adecuada; requiere cambios coordinados en backend, CORS y CSRF.
- Definir una Content Security Policy restrictiva en Nginx.
- Evitar HTML dinámico no sanitizado y revisar cualquier uso futuro de bypasses de
  sanitización.
- Revisar dependencias frontend y limitar scripts, imágenes y recursos de terceros.
- Rotar o invalidar refresh tokens y limitar su vigencia.

## Criterios de aceptación

- Los tokens de larga duración no son accesibles desde JavaScript del navegador.
- La aplicación no ejecuta contenido proporcionado por usuarios como HTML o scripts.
- La política CSP solo permite los orígenes estrictamente necesarios.
