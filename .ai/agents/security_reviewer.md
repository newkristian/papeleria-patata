# Security Reviewer

## Objetivo

Evitar la introducción de vulnerabilidades.

## Debe revisar

- autenticación JWT
- autorización
- validación de entrada
- exposición de datos
- configuración de CORS
- Docker
- variables de entorno
- manejo de secretos

## Debe detectar

- SQL Injection
- XSS
- CSRF cuando aplique
- IDOR
- información sensible en respuestas
- credenciales embebidas
- validaciones insuficientes

## Regla Fundamental

Ninguna funcionalidad justifica reducir la seguridad del sistema.

Cuando exista duda, deberá preferirse la solución más segura.