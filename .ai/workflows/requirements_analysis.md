# Análisis de Requerimientos

## Regla obligatoria

Antes de realizar cualquier modificación de código, el agente deberá analizar
el requerimiento.

Está prohibido comenzar directamente la implementación.

---

# Objetivos del análisis

El agente deberá determinar:

- qué se solicita;
- qué comportamiento se espera;
- qué componentes están involucrados;
- qué código existente puede reutilizarse;
- qué modificaciones serán necesarias;
- qué riesgos existen;
- qué dependencias hay entre tareas;
- si se requiere modificar backend, frontend, base de datos o infraestructura.

---

# Inspección del proyecto

Antes de proponer una solución, revisar el código relacionado existente.

Buscar:

- funcionalidades similares;
- endpoints existentes;
- servicios reutilizables;
- componentes reutilizables;
- modelos existentes;
- convenciones ya utilizadas.

No diseñar una nueva solución sin revisar primero cómo resuelve el proyecto
problemas similares.

---

# Detección de problemas

Durante el análisis, el agente deberá identificar:

- requerimientos contradictorios;
- información faltante;
- comportamientos ambiguos;
- posibles bugs;
- vulnerabilidades;
- inconsistencias con el dominio;
- incompatibilidades con arquitectura existente.

---

# Información insuficiente

Si falta información necesaria para tomar una decisión correcta, el agente
deberá solicitarla antes de implementar.

No deberá asumir silenciosamente reglas importantes del negocio.

---

# Múltiples soluciones

Cuando existan varias alternativas técnicamente razonables, el agente deberá
presentar:

1. alternativas principales;
2. ventajas;
3. desventajas;
4. impacto aproximado;
5. recomendación.

El propietario tomará la decisión final cuando exista una diferencia
significativa entre alternativas.

---

# Plan de implementación

Después del análisis deberá generarse un listado ordenado de tareas.

Ejemplo:

1. Crear migración para agregar stock mínimo.
2. Modificar entidad Product.
3. Modificar modelo de dominio.
4. Modificar caso de uso.
5. Modificar API.
6. Verificar endpoints.
7. Implementar consumo en Angular.
8. Crear pantalla.
9. Verificar flujo completo.

Las tareas deberán ser suficientemente pequeñas para poder revisarse
individualmente.

---

# Documentación del requerimiento

Cuando una funcionalidad sea aprobada y tenga suficiente alcance para requerir
seguimiento, deberá existir un documento dentro de:

`docs/requerimientos/<modulo>/<FEATURE>.md`

Ese documento representa la fuente funcional del requerimiento.

Los archivos dentro de `.ai/` definen cómo trabajar sobre el requerimiento,
pero no deberán utilizarse para almacenar especificaciones funcionales.

---

# Aprobación

El agente deberá presentar el análisis y el plan al propietario.

No deberá modificar código hasta recibir aprobación explícita.

Si el propietario modifica el alcance, deberá actualizarse el plan antes de
continuar.