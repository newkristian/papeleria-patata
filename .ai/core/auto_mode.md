# Modo Automático (Auto Mode)

## Objetivo

Permitir que el agente encadene tareas **estrictamente de bajo riesgo** sin
detenerse a esperar aprobación entre cada una, sin debilitar ninguna otra
regla del proyecto — en particular la prohibición de commits automáticos
(ver `.ai/README.md`) y la prioridad de seguridad (ver `priorities.md`).

Esta sección es una excepción acotada a la **Fase 5 — Pausa obligatoria**
de `.ai/workflows/task_lifecycle.md`. Todo lo demás del flujo obligatorio
sigue vigente sin cambios.

---

## Alcance permitido sin pausa

El agente puede avanzar sin esperar aprobación explícita entre tareas
**únicamente** cuando cada tarea de la secuencia sea, sin excepción:

- cambios puramente mecánicos: formateo, orden de imports, renombrados
  triviales, correcciones ortográficas;
- documentación que no introduce ni modifica reglas de negocio;
- ejecución de pruebas ya existentes, sin modificarlas, para verificar el
  estado del código;
- correcciones de estilo o lint que no alteran comportamiento.

Si hay duda sobre si una tarea es de bajo riesgo, **no lo es**: se aplica
el flujo normal con pausa.

---

## Excluido de auto mode (siempre requiere pausa y aprobación explícita)

- Cualquier cambio en backend (Spring Boot, Java, JPA, endpoints).
- Cualquier cambio relacionado con seguridad: autenticación, autorización,
  JWT, CORS, datos sensibles o archivos de usuarios (ver `rules/security.md`).
- Cualquier cambio en base de datos, migraciones Flyway o modelos de datos
  (ver `rules/database.md`).
- Cualquier cambio en contratos de API (request/response) consumidos por
  frontend o terceros (ver `rules/api_rest.md`).
- Cualquier cambio de arquitectura, dependencias o configuración crítica
  (ver `modification_policy.md`).
- `git commit`, `git push`, merges, tags o cambios de rama — esto nunca es
  automático bajo ninguna circunstancia.
- Cualquier tarea con información faltante, ambigüedad o varias soluciones
  razonables (ver `decision_rules.md`).

---

## Comportamiento del agente en auto mode

Aunque se encadenen tareas sin pausar, el agente deberá:

- informar, al terminar cada tarea, qué se modificó y qué archivos fueron
  afectados (igual que en el flujo normal, solo que sin detenerse a
  esperar aprobación antes de continuar con la siguiente tarea de bajo
  riesgo);
- detenerse inmediatamente si una tarea que parecía de bajo riesgo resulta
  requerir tocar lógica de negocio, seguridad, backend, base de datos o
  contratos de API, y volver al flujo normal con pausa para esa tarea;
- nunca ejecutar `git commit` ni `git push` por su cuenta, incluso en auto
  mode.

---

## Activación

Auto mode no está activo por defecto. El propietario debe indicarlo
explícitamente para una tarea o sesión concreta. Fuera de esa indicación
explícita, se aplica siempre el flujo normal descrito en
`.ai/workflows/task_lifecycle.md`.
