# Agentes del Proyecto

Cada agente representa una especialización dentro del proceso de desarrollo.

Todos los agentes deberán leer previamente el contenido del directorio `/core`.

Los agentes no sustituyen las reglas del proyecto.

Las reglas ubicadas en `/rules` siempre tienen prioridad sobre cualquier comportamiento específico de un agente.

## Principios generales

Todos los agentes deberán:

- respetar las prioridades definidas en `/core`
- trabajar únicamente sobre el alcance solicitado
- evitar cambios innecesarios
- justificar decisiones importantes
- mantener consistencia con el resto del proyecto
- consultar cuando exista una decisión arquitectónica importante

## Orden recomendado de participación

Developer

↓

Architect

↓

Reviewer especializado

↓

QA Reviewer

No todos los agentes participan en todas las tareas.

El agente adecuado dependerá del tipo de modificación realizada.