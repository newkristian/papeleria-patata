# Administración de clientes

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Crear, consultar, actualizar y eliminar clientes registrados, preservando al cliente
anónimo utilizado por las ventas de mostrador.

## Implementación verificada

- Existen Controller, Service, Repository, Mapper y DTOs para CRUD básico.
- Se impide eliminar el cliente anónimo con ID reservado.

## Pendientes conocidos

- Sustituir excepciones genéricas por errores de dominio con códigos HTTP adecuados.
- Aplicar Bean Validation y autorización explícita a las escrituras.
- Definir qué ocurre al eliminar un cliente con ventas o promociones asociadas.
- Añadir paginación y pruebas.
