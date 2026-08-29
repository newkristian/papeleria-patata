# Administración de proveedores

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Crear, consultar, actualizar y eliminar proveedores asociados al catálogo.

## Implementación verificada

- Existen Controller, Service, Repository, Mapper y DTOs para CRUD básico.
- El porcentaje de comisión utiliza `BigDecimal` y `NUMERIC(5, 2)`, con rango de
  cero a cien protegido en el contrato y en la base de datos.

## Pendientes conocidos

- Aplicar Bean Validation y autorización explícita a escrituras.
- Sustituir excepciones genéricas y definir conflictos por productos relacionados.
- Validar RFC y datos de contacto; añadir paginación y pruebas del CRUD.
