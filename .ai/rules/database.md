# Base de Datos

## Stack

- PostgreSQL
- Spring Data JPA
- Hibernate
- Flyway

---

# Migraciones

Todo cambio estructural de base de datos deberá realizarse mediante Flyway.

Nunca modificar manualmente el esquema de producción como mecanismo normal de
despliegue.

Una migración aplicada no deberá modificarse posteriormente.

Crear una nueva migración para realizar correcciones.

En producción, Hibernate deberá usar `ddl-auto=validate` o `none`. Nunca utilizar
`create`, `create-drop` o `update` como mecanismo de evolución del esquema
productivo; Flyway es la única fuente de cambios estructurales.

---

# JPA

Las entidades JPA representan persistencia.

No deben utilizarse como contratos HTTP.

Mantener relaciones únicamente cuando sean necesarias.

Evitar relaciones bidireccionales por defecto.

---

# Fetching

Evitar `EAGER` indiscriminado.

Preferir cargas explícitas según las necesidades del caso de uso.

Revisar posibles problemas N+1 cuando se consulten relaciones.

No realizar optimizaciones complejas sin evidencia de un problema.

---

# Índices

Crear índices cuando exista una necesidad clara derivada de:

- búsquedas frecuentes;
- claves foráneas relevantes;
- restricciones únicas;
- consultas importantes.

No crear índices especulativamente.

---

# Integridad

Preferir restricciones de base de datos para proteger invariantes estructurales:

- NOT NULL
- UNIQUE
- FOREIGN KEY

La validación de aplicación no sustituye la integridad de la base de datos.

---

# Transacciones

Las operaciones que modifican múltiples datos que deben mantenerse consistentes
deberán ejecutarse dentro de una misma transacción.

Esto es especialmente importante en operaciones del POS relacionadas con:

- ventas;
- pagos;
- movimientos de inventario;
- movimientos de caja.

---

# Dinero

Nunca utilizar `float` o `double` para valores monetarios.

En Java utilizar `BigDecimal`.

En PostgreSQL utilizar tipos numéricos de precisión adecuada.

Las reglas de redondeo deberán ser explícitas cuando sean necesarias.
