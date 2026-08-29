# Revisión y Commit

## Propósito

Mantener al propietario en control de cada etapa de implementación.

---

# Regla

El agente no deberá encadenar múltiples tareas del plan sin permitir revisión
entre ellas.

---

# Después de cada tarea

El agente deberá informar:

- tarea completada;
- archivos modificados;
- decisiones importantes;
- desviaciones del plan, si existen;
- verificaciones realizadas;
- problemas pendientes.

---

# Pausa

Después del informe deberá detenerse.

El propietario podrá:

- revisar código;
- solicitar correcciones;
- aprobar;
- realizar commit.

---

# Commit

El commit corresponde al propietario.

El agente no deberá realizar automáticamente:

- commit;
- push;
- merge;
- creación de tags;

salvo solicitud explícita.

---

# Continuación

La siguiente tarea deberá comenzar únicamente después de que el propietario
indique que puede continuar.

---

# Correcciones durante revisión

Si el propietario solicita cambios sobre la tarea actual:

1. mantener la tarea actual abierta;
2. realizar la corrección;
3. volver a presentar el resultado;
4. esperar nueva revisión.

No avanzar a la siguiente tarea hasta cerrar la actual.