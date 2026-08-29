# Despliegue en VPS

**Estado:** Aprobado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Desplegar el stack Alfa en un VPS con configuración reproducible y segura.

## Implementación verificada

La base Docker existe, pero no hay script `deploy.sh` ni configuración específica
del servidor.

## Pendientes conocidos

- Definir dominio, TLS, firewall, respaldos, secretos y estrategia de actualización.
- Crear y verificar el procedimiento de despliegue y recuperación.
