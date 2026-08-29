# Ciclo de Vida de una Tarea

Toda tarea que implique cambios de código deberá seguir este workflow.

---

# Fase 1 — Análisis

Aplicar:

`requirements_analysis.md`

No modificar código durante esta fase.

---

# Fase 2 — Propuesta

Presentar al propietario:

- interpretación del requerimiento;
- posibles problemas detectados;
- solución propuesta;
- archivos o módulos afectados;
- tareas ordenadas.

Esperar aprobación.

---

# Fase 3 — Implementación

Después de recibir aprobación, implementar únicamente la primera tarea
pendiente.

No ejecutar automáticamente todo el plan.

---

# Fase 4 — Verificación de tarea

Después de completar una tarea:

- verificar compilación cuando corresponda;
- revisar el cambio realizado;
- informar qué se modificó;
- informar cualquier problema detectado.

---

# Fase 5 — Pausa obligatoria

Después de cada tarea deberá realizarse una pausa.

El propietario deberá poder:

- revisar el código;
- solicitar cambios;
- realizar el commit;
- autorizar continuar.

El agente no deberá realizar commits salvo instrucción explícita.

---

# Fase 6 — Continuación

Una vez aprobado el cambio anterior, continuar con la siguiente tarea del plan.

Repetir:

Implementación

↓

Verificación

↓

Revisión del propietario

↓

Commit

↓

Siguiente tarea

---

# Cambios inesperados

Si durante la implementación aparece nueva información que modifica
significativamente el plan:

1. detener la tarea;
2. explicar el descubrimiento;
3. actualizar el análisis;
4. proponer cambios al plan;
5. esperar decisión del propietario.

No continuar utilizando un plan que ya no representa correctamente el trabajo.