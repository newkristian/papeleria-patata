# API REST

## Principios

Las APIs deberán utilizar recursos y semántica HTTP correctamente.

Preferir URLs basadas en sustantivos.

Preferir:

GET /api/products
GET /api/products/{id}
POST /api/products
PUT /api/products/{id}
DELETE /api/products/{id}

Evitar:

POST /api/getProducts
POST /api/createProduct

---

# Métodos HTTP

GET:
consultar información.

POST:
crear recursos u operaciones no idempotentes.

PUT:
actualizar o reemplazar un recurso de manera idempotente.

PATCH:
actualizaciones parciales cuando aporten una ventaja clara.

DELETE:
eliminar recursos.

---

# DTOs

Nunca exponer directamente entidades JPA.

Separar contratos HTTP de persistencia.

Utilizar Request y Response distintos cuando representen responsabilidades
diferentes.

No crear DTOs innecesarios cuando dos operaciones compartan realmente el mismo
contrato.

---

# Validación

Validar entradas utilizando Bean Validation.

Ejemplo:

```java
public record CreateProductRequest(
    @NotBlank String name,
    @Positive BigDecimal price
) {}
```

El backend siempre debe validar los datos aunque el frontend también los
valide.

---

# Códigos HTTP

Utilizar códigos HTTP semánticamente correctos.

Ejemplos:

- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error

No devolver `200 OK` para representar errores.

---

# Paginación

Los endpoints que potencialmente puedan devolver cantidades grandes de datos
deberán considerar paginación.

No agregar paginación a recursos pequeños únicamente por convención.

---

# Compatibilidad

Evitar romper contratos existentes innecesariamente.

Los cambios incompatibles deberán identificarse antes de implementarse.

---

# OpenAPI

La especificación OpenAPI generada deberá representar el contrato HTTP vigente.
Cuando cambien rutas, DTOs, validaciones, autenticación o respuestas relevantes,
deberá revisarse también su documentación.

Documentar como mínimo cuando no puedan inferirse correctamente:

- propósito de la operación;
- esquemas Request y Response;
- autenticación Bearer;
- códigos de éxito y errores de negocio relevantes;
- restricciones que el tipo por sí solo no comunique.

Los esquemas públicos deberán usar DTOs y nunca entidades JPA.

Swagger UI facilita la exploración manual, pero no sustituye la autorización real ni
las pruebas automatizadas. Su exposición deberá decidirse explícitamente por
ambiente; no hacer públicas sus rutas en producción por simple conveniencia.
