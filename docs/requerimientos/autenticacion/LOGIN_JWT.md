# Inicio de sesión con JWT

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Autenticar empleados y entregar tokens firmados para consumir la API protegida.

## Implementación verificada

- El backend expone `POST /api/v1/auth/login` y genera `accessToken` y `refreshToken`.
- El frontend incluye formulario reactivo, interceptor JWT y guard de ruta.

## Pendientes conocidos

- El frontend espera `token`, pero el backend responde `accessToken`; actualmente
  el token no se almacena y el flujo protegido no puede completarse.
- Definir tratamiento de expiración, respuestas 401 y cierre de sesión automático.

## Criterios de aceptación

- Credenciales válidas permiten entrar al POS y consumir un endpoint protegido.
- Credenciales inválidas no almacenan tokens y muestran un error comprensible.
