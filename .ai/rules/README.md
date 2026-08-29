# Reglas Técnicas del Proyecto

Este directorio contiene las reglas técnicas obligatorias para el desarrollo del sistema POS.

Las reglas complementan los principios establecidos en `.ai/core/`.

## Orden de precedencia

Ante un conflicto se deberá respetar:

1. `.ai/core/priorities.md`
2. `.ai/core/decision_rules.md`
3. Reglas de seguridad
4. Reglas de arquitectura
5. Reglas específicas de tecnología
6. Convenciones de código

Una regla nunca deberá aplicarse mecánicamente si provoca una vulnerabilidad,
error funcional o degradación objetiva del sistema.

En ese caso deberá seguirse el procedimiento de excepción definido en
`.ai/core/decision_rules.md`.

## Reglas disponibles

| Archivo | Responsabilidad |
|---|---|
| `architecture.md` | Arquitectura Hexagonal Simplificada |
| `backend.md` | Spring Boot 4 y Java 21 |
| `frontend.md` | Angular 22 y Tailwind CSS |
| `api_rest.md` | Diseño de API REST |
| `database.md` | PostgreSQL, JPA y Flyway |
| `security.md` | Seguridad, autenticación y autorización |
| `coding_standards.md` | Convenciones generales de código |
| `exceptions.md` | Manejo de errores |
| `logging.md` | Logging y observabilidad |
| `testing.md` | Estrategia de pruebas |
| `docker.md` | Contenedores y despliegue |
| `dependencies.md` | Gestión de dependencias |
| `documentation.md` | Código y documentación |