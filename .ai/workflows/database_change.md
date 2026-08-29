# Cambios de Base de Datos

## Análisis

Antes de modificar el esquema determinar:

- datos existentes;
- compatibilidad;
- impacto en JPA;
- impacto en API;
- posible pérdida de información;
- necesidad de migrar valores existentes.

---

# Orden

Cuando una funcionalidad requiera cambiar base de datos:

1. Diseñar el cambio.
2. Crear migración Flyway.
3. Actualizar persistencia.
4. Actualizar dominio/aplicación.
5. Actualizar API.
6. Verificar backend.
7. Continuar con frontend.

---

# Producción

Considerar siempre que pueden existir datos reales.

No asumir que una tabla está vacía.

---

# Migraciones

Nunca modificar una migración ya aplicada.

Crear una nueva migración.

---

# Operaciones destructivas

Cambios como:

- DROP TABLE;
- DROP COLUMN;
- eliminación masiva;
- transformación irreversible;
- cambio incompatible de tipo;

deberán marcarse explícitamente como riesgosos.

El agente deberá informar el riesgo antes de implementarlos.

---

# Valores obligatorios nuevos

Si se agrega una columna NOT NULL a una tabla existente, deberá definirse cómo
se migrarán los registros existentes.

No crear una migración que pueda fallar sobre datos de producción conocidos.