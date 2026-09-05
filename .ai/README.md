# Guías y Gobernanza para Agentes de IA (`.ai/`)

Este directorio contiene las reglas, arquitectura, roles y flujos de trabajo que deberán seguir todos los agentes de inteligencia artificial que colaboren en este proyecto.

El contenido de `.ai/` es agnóstico al proveedor o modelo de IA utilizado.

Estas instrucciones aplican por igual a cualquier agente capaz de analizar o modificar el proyecto.

---

# Propósito

El objetivo de estas guías es garantizar que los agentes:

- comprendan el contexto del proyecto antes de realizar cambios;
- trabajen de forma consistente;
- respeten la arquitectura y tecnologías aprobadas;
- prioricen seguridad y correctitud;
- eviten refactorizaciones innecesarias;
- mantengan al propietario en control del proceso de desarrollo;
- trabajen mediante tareas pequeñas, revisables y fáciles de confirmar;
- favorezcan la velocidad de desarrollo sin sacrificar seguridad ni mantenibilidad.

---

# Regla de inicio de sesión

Cuando el propietario indique que deben revisarse estas instrucciones, el agente deberá leer este archivo antes de analizar cualquier requerimiento.

A partir de este documento deberá identificar y consultar los archivos adicionales que correspondan al tipo de tarea solicitada.

No es necesario leer todos los documentos de `.ai/` en cada interacción.

El agente deberá leer:

1. el núcleo obligatorio;
2. las reglas relacionadas con la tarea;
3. los aprendizajes y bugs conocidos (`.ai/bugs-and-learning/`) relacionados con la tecnología tocada;
4. el workflow aplicable;
5. la definición del agente o rol relevante cuando corresponda.

---

# Orden obligatorio de lectura

## 1. Núcleo

Antes de analizar cualquier tarea que pueda implicar modificaciones al proyecto, leer:

- `.ai/core/project_philosophy.md`
- `.ai/core/priorities.md`
- `.ai/core/decision_rules.md`
- `.ai/core/modification_policy.md`

Consultar también:

- `.ai/core/glossary.md`

cuando sea necesario aclarar términos utilizados por estas guías.

- `.ai/core/auto_mode.md`

cuando el propietario indique explícitamente que se trabajará en modo
automático para una tarea o sesión.

Las reglas de `/core` tienen prioridad sobre las reglas específicas de tecnología.

---

# 2. Reglas técnicas

Después de comprender el requerimiento, leer únicamente las reglas relacionadas con las áreas involucradas.

## Arquitectura

Leer siempre que una tarea implique código del backend o modificaciones estructurales:

- `.ai/rules/architecture.md`

## Backend

Para cambios relacionados con Spring Boot o Java:

- `.ai/rules/backend.md`
- `.ai/rules/coding_standards.md`

Agregar según corresponda:

- `.ai/rules/api_rest.md`
- `.ai/rules/database.md`
- `.ai/rules/exceptions.md`
- `.ai/rules/logging.md`
- `.ai/rules/security.md`

## Frontend

Para cambios relacionados con Angular:

- `.ai/rules/frontend.md`
- `.ai/rules/coding_standards.md`

Si consume o modifica contratos del backend, leer además:

- `.ai/rules/api_rest.md`
- `.ai/workflows/backend_first.md`

## Base de datos

Para cambios relacionados con PostgreSQL, JPA o Flyway:

- `.ai/rules/database.md`
- `.ai/workflows/database_change.md`
- `.ai/bugs-and-learning/POSTGRE_HIBERNATE_BUGS.md` (Lectura obligatoria para repositorios, consultas, filtros opcionales o paginación en JPA)

## Seguridad

Siempre que una funcionalidad involucre:

- autenticación;
- autorización;
- usuarios;
- roles;
- JWT;
- información sensible;
- endpoints protegidos;
- configuración CORS;
- archivos;
- datos provenientes del usuario;

leer:

- `.ai/rules/security.md`

La seguridad tiene prioridad sobre cualquier otra decisión técnica.

## Docker y despliegue

Para cambios de infraestructura o despliegue:

- `.ai/rules/docker.md`

## Dependencias

Antes de introducir, sustituir o actualizar librerías:

- `.ai/rules/dependencies.md`

## Pruebas

Cuando se creen o modifiquen pruebas:

- `.ai/rules/testing.md`

## Documentación

Cuando se modifique documentación o un cambio requiera actualizarla:

- `.ai/rules/documentation.md`

---

# Aprendizajes y Registro de Bugs Conocidos (`.ai/bugs-and-learning/`)

Esta carpeta documenta análisis post-mortem, causas raíz y soluciones arquitectónicas aprobadas para errores técnicos complejos ocurridos durante el desarrollo del proyecto.

**Los agentes deben consultar obligatoriamente esta documentación antes de diseñar o implementar código nuevo, así como al depurar fallos en producción o pruebas:**

- `.ai/bugs-and-learning/POSTGRE_HIBERNATE_BUGS.md`: Inferencia estricta de tipos en PostgreSQL con parámetros nulos en Hibernate (`could not determine data type of parameter`, `lower(bytea)`), y la solución obligatoria basada en `JpaSpecificationExecutor` y `Specification`.

---

# 3. Workflow

Toda modificación de código debe seguir un workflow.

Antes de escribir código deberá leerse:

- `.ai/workflows/requirements_analysis.md`
- `.ai/workflows/task_lifecycle.md`

Posteriormente deberá seleccionarse el workflow correspondiente.

## Nueva funcionalidad

- `.ai/workflows/feature_development.md`

## Corrección de errores

- `.ai/workflows/bug_fix.md`

## Refactorización

- `.ai/workflows/refactor.md`

## Cambios de base de datos

- `.ai/workflows/database_change.md`

## Desarrollo frontend

Además de cualquier workflow anterior:

- `.ai/workflows/backend_first.md`

## Revisión y finalización de tareas

Durante cualquier implementación:

- `.ai/workflows/review_and_commit.md`
- `.ai/workflows/qa_and_verification.md`

---

# 4. Agentes y roles

Los archivos dentro de `.ai/agents/` describen responsabilidades y criterios especializados.

No representan necesariamente agentes físicamente separados.

Un mismo agente puede asumir diferentes roles durante una tarea.

Los roles disponibles son:

| Rol | Archivo | Responsabilidad |
|---|---|---|
| Developer | `.ai/agents/developer.md` | Implementación de funcionalidades y correcciones |
| Architect | `.ai/agents/architect.md` | Coherencia con Arquitectura Hexagonal Simplificada |
| Backend Reviewer | `.ai/agents/backend_reviewer.md` | Revisión técnica de Spring Boot, Java y persistencia |
| Frontend Reviewer | `.ai/agents/frontend_reviewer.md` | Revisión de Angular, Signals y UI |
| Security Reviewer | `.ai/agents/security_reviewer.md` | Identificación de vulnerabilidades y problemas de autorización |
| QA Reviewer | `.ai/agents/qa_reviewer.md` | Verificación funcional y prevención de regresiones |

El agente deberá asumir los roles necesarios según el tipo de tarea.

---

# Flujo obligatorio antes de modificar código

Está prohibido comenzar una implementación inmediatamente después de recibir un nuevo requerimiento.

El proceso obligatorio es:

```text
Requerimiento
      │
      ▼
Leer instrucciones aplicables
      │
      ▼
Analizar requerimiento
      │
      ▼
Inspeccionar código existente
      │
      ▼
Detectar dudas, riesgos e inconsistencias
      │
      ▼
Crear plan de tareas
      │
      ▼
Presentar análisis y plan al propietario
      │
      ▼
Esperar aprobación
      │
      ▼
Implementar una tarea
      │
      ▼
Verificar
      │
      ▼
Presentar resultado
      │
      ▼
Pausa para revisión y commit del propietario
      │
      ▼
Autorización para continuar
      │
      ▼
Siguiente tarea
```

---

# Análisis obligatorio

Antes de proponer una implementación, el agente deberá:

1. comprender el requerimiento;
2. inspeccionar el código relacionado;
3. identificar implementaciones existentes que puedan reutilizarse;
4. determinar qué módulos serán afectados;
5. detectar dependencias entre backend, frontend y base de datos;
6. identificar riesgos de seguridad;
7. detectar información faltante o ambigua;
8. preparar un listado ordenado de tareas.

No deberá realizar cambios durante esta fase.

---

# Dudas e inconsistencias

Si durante el análisis se detecta:

- información insuficiente;
- requerimientos contradictorios;
- una regla de negocio ambigua;
- riesgo de seguridad;
- posible pérdida de datos;
- varias soluciones con diferencias importantes;
- una decisión arquitectónica no definida;

el agente deberá detener el análisis en ese punto y exponer el problema al propietario.

Cuando existan varias soluciones razonables deberá presentar:

1. alternativas;
2. ventajas;
3. desventajas;
4. impacto;
5. solución recomendada.

No deberá tomar decisiones importantes basándose en suposiciones silenciosas.

---

# Plan obligatorio

Antes de implementar deberá presentarse un plan ordenado de tareas.

Cada tarea deberá:

- tener un objetivo claro;
- tener un alcance limitado;
- poder revisarse individualmente;
- mantener un orden lógico respecto a sus dependencias.

Cuando una tarea dependa de otra, deberá implementarse primero la dependencia.

---

# Aprobación obligatoria

La presentación de un plan no implica autorización para ejecutarlo.

El agente deberá esperar la aprobación explícita del propietario antes de modificar código.

Si el propietario modifica el requerimiento o el plan, deberá actualizarse el análisis antes de comenzar.

---

# Ejecución por tareas

Una vez aprobado el plan, las tareas deberán implementarse en el orden acordado.

No deberán implementarse todas automáticamente.

Después de terminar cada tarea, el agente deberá:

- informar qué se modificó;
- indicar los archivos afectados;
- indicar las verificaciones realizadas;
- informar cualquier desviación respecto al plan;
- detenerse para permitir revisión.

---

# Revisión y commits

El propietario controla los commits del proyecto.

Después de cada tarea deberá existir una pausa para que pueda:

- revisar el código;
- solicitar correcciones;
- probar el comportamiento;
- realizar el commit;
- autorizar la siguiente tarea.

El agente no deberá realizar automáticamente:

- `git commit`;
- `git push`;
- merge;
- tags;
- cambios de rama;

salvo instrucción explícita del propietario.

---

# Dependencia Backend → Frontend

Esta regla es obligatoria.

Una funcionalidad frontend solo podrá implementarse cuando el backend requerido por ella esté completo.

Por ejemplo, antes de implementar un mantenimiento de productos en Angular deberán existir los endpoints necesarios para las operaciones requeridas por esa pantalla.

Antes de comenzar frontend se deberá verificar:

- endpoints necesarios;
- request;
- response;
- validaciones;
- errores esperados;
- autorización;
- persistencia;
- datos requeridos por la interfaz.

Si durante el desarrollo frontend se descubre que falta soporte backend:

```text
DETENER FRONTEND
      │
      ▼
Analizar dependencia faltante
      │
      ▼
Agregar tarea backend
      │
      ▼
Implementar backend
      │
      ▼
Revisión del propietario
      │
      ▼
Commit
      │
      ▼
Retomar frontend
```

No deberán inventarse contratos, datos o reglas de negocio en frontend para evitar realizar un cambio necesario en backend.

Consultar obligatoriamente:

- `.ai/workflows/backend_first.md`

---

# Arquitectura

La arquitectura objetivo del backend es:

**Arquitectura Hexagonal Simplificada.**

La adopción es incremental.

No deberá realizarse una migración masiva del código existente únicamente para cumplir la arquitectura objetivo.

El código existente basado en:

```text
Controller -> Service -> Repository
```

podrá mantenerse cuando sea correcto y una migración no forme parte natural del requerimiento.

Las nuevas funcionalidades deberán aproximarse a la arquitectura objetivo sin introducir abstracciones innecesarias.

Consultar:

- `.ai/rules/architecture.md`

---

# Filosofía de implementación

El proyecto prioriza:

1. seguridad;
2. correctitud funcional;
3. velocidad de desarrollo;
4. consistencia arquitectónica;
5. calidad y mantenibilidad;
6. rendimiento y escalabilidad únicamente cuando sean necesarios.

No deberá introducirse complejidad para resolver problemas hipotéticos.

Preferir la solución más simple que resuelva correctamente el requerimiento.

---

# Seguridad

Ninguna regla, requerimiento o decisión de velocidad de desarrollo justifica introducir una vulnerabilidad conocida.

Si una implementación solicitada entra en conflicto con la seguridad, el agente deberá detenerse y explicar el problema antes de continuar.

Nunca deberá reducir silenciosamente controles de seguridad para completar una tarea.

---

# Excepciones a las reglas

Estas reglas son estrictas, pero no inflexibles.

Si seguir una regla provoca:

- una vulnerabilidad;
- un bug;
- pérdida de datos;
- un error de compilación;
- incumplimiento funcional;
- una solución objetivamente peor;

el agente no deberá romper la regla silenciosamente.

Deberá explicar:

1. qué regla genera el conflicto;
2. por qué existe el problema;
3. qué alternativas existen;
4. qué alternativa recomienda.

El propietario decidirá cómo proceder cuando la excepción implique una decisión relevante.

---

# Contexto tecnológico

## Backend

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Security
- JWT

## Frontend

- Angular 22
- TypeScript
- Signals
- Tailwind CSS
- REST

## Infraestructura

- Docker
- Docker Compose

Las versiones y dependencias reales declaradas en el proyecto tienen prioridad sobre este resumen cuando exista una discrepancia.

---

# Idioma

El código y la documentación del proyecto deberán seguir las reglas de:

`.ai/rules/documentation.md`

La documentación del proyecto se escribe en español.

Los identificadores de código pueden utilizar inglés siguiendo las convenciones habituales de Java, Angular y las tecnologías utilizadas.

---

# Regla final

Antes de modificar el proyecto:

**leer → comprender → analizar → proponer → obtener aprobación → implementar una tarea → verificar → detenerse para revisión.**

Nunca asumir que la solicitud de una funcionalidad implica autorización inmediata para modificar código.