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
- El JWT solo lleva `sub` (username); `AuthResponse` no expone nombre, rol ni tienda
  del usuario autenticado. Hoy el frontend no tiene forma de saber quién inició sesión
  ni su rol sin decodificar el token de forma no soportada. No bloquea ninguna
  sub-tarea de `docs/requerimientos/pos/` conocida hasta ahora (detectado durante la
  Tarea 7 de `TAREAS_VENTAS.md`; ver `docs/trabajo-actual/TAREAS_POS.md`), pero limita
  cualquier UI futura que necesite mostrar "Cajero: <nombre>" o condicionar algo al rol
  del usuario en sesión sin hacer una llamada adicional. Antes de implementarlo, decidir
  entre: (a) agregar `nombre`/`rol`/`tiendaId`/`tiendaNombre` a `AuthResponse`, o (b) un
  endpoint dedicado `GET /api/v1/auth/me` (o similar) que el frontend consulte tras el
  login.

## Criterios de aceptación

- Credenciales válidas permiten entrar al POS y consumir un endpoint protegido.
- Credenciales inválidas no almacenan tokens y muestran un error comprensible.
