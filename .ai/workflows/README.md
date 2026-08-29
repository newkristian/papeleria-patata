# Flujos de Trabajo

Este directorio define el proceso obligatorio que deberá seguir cualquier agente
antes, durante y después de modificar el proyecto.

Los workflows no sustituyen las reglas definidas en `.ai/core/` ni `.ai/rules/`.

Su objetivo es establecer el orden de trabajo.

---

# Principio fundamental

Ningún cambio de código deberá comenzar directamente.

Toda modificación deberá seguir como mínimo:

Análisis

↓

Plan de tareas

↓

Revisión del propietario

↓

Aprobación

↓

Implementación por tareas

↓

Revisión entre tareas

↓

Commit realizado por el propietario

↓

Continuación

---

# Workflows disponibles

| Archivo | Responsabilidad |
|---|---|
| `requirements_analysis.md` | Análisis previo obligatorio |
| `task_lifecycle.md` | Ciclo completo de cualquier tarea |
| `feature_development.md` | Desarrollo de nuevas funcionalidades |
| `backend_first.md` | Dependencia obligatoria Backend → Frontend |
| `bug_fix.md` | Corrección de errores |
| `refactor.md` | Refactorizaciones |
| `database_change.md` | Cambios de base de datos |
| `review_and_commit.md` | Pausas, revisión y commits |
| `qa_and_verification.md` | Verificación final |