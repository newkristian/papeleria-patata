# Refactorización

## Regla

La refactorización no deberá realizarse de manera incidental a gran escala.

---

# Antes de refactorizar

Definir:

- problema actual;
- beneficio esperado;
- alcance;
- riesgos;
- partes afectadas.

---

# Arquitectura

Las migraciones hacia Arquitectura Hexagonal Simplificada deberán ser
incrementales.

No migrar módulos completos únicamente porque presentan arquitectura antigua.

---

# Código existente

Código basado en:

Controller -> Service -> Repository

puede continuar existiendo mientras sea correcto y mantenible.

Cuando se modifique naturalmente una parte del sistema podrá aprovecharse la
tarea para mejorar su arquitectura si el cambio es pequeño y está directamente
relacionado.

---

# Comportamiento

Una refactorización no deberá cambiar el comportamiento funcional salvo que ese
cambio forme explícitamente parte del requerimiento.

---

# Ejecución

Dividir grandes refactorizaciones en tareas independientes.

Después de cada tarea aplicar:

`review_and_commit.md`