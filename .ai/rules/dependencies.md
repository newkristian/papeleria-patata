# Dependencias

## Principio

Cada dependencia aumenta el costo de mantenimiento y la superficie de ataque.

No agregar una dependencia cuando la plataforma o una dependencia existente
resuelva razonablemente el problema.

---

# Nuevas dependencias

Antes de incorporar una dependencia evaluar:

- necesidad;
- mantenimiento;
- compatibilidad;
- seguridad;
- licencia cuando corresponda.

No introducir frameworks para resolver problemas pequeños.

---

# Versiones

No actualizar versiones mayores como parte de una tarea no relacionada.

Las actualizaciones por vulnerabilidades de seguridad tienen prioridad.

Mantener y respetar los archivos de bloqueo y wrappers del repositorio para obtener
instalaciones reproducibles. No sustituir `npm ci` por una instalación que modifique
el lockfile dentro de CI o de un build Docker.

---

# Backend

Preferir capacidades de:

- Java
- Spring
- Hibernate

antes de introducir librerías adicionales para funcionalidades ya cubiertas.

---

# Frontend

Preferir:

- Angular
- TypeScript
- Tailwind CSS

antes de introducir nuevas librerías.

No incorporar librerías completas para utilizar uno o dos componentes triviales.

# Dependencias funcionales entre capas

El frontend depende funcionalmente del backend.

No deberá desarrollarse una funcionalidad frontend cuyo contrato backend no se
encuentre completo.

Aplicar obligatoriamente:

`.ai/workflows/backend_first.md`
