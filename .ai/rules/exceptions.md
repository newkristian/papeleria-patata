# Manejo de Excepciones

## Principios

Los errores deberán manejarse de forma consistente.

Las excepciones no deben utilizarse como flujo normal de control.

---

# Excepciones de dominio

Crear excepciones específicas cuando representen errores significativos.

Ejemplos conceptuales:

- ProductNotFoundException
- InsufficientStockException
- CashRegisterClosedException

Deberán extender `RuntimeException` directa o indirectamente.

---

# Manejo global

Centralizar la conversión de excepciones a respuestas HTTP mediante:

```java
@RestControllerAdvice
```

y:

```java
@ExceptionHandler
```

Los Controllers no deberán repetir manejo de excepciones.

---

# Respuesta de error

Mantener una estructura consistente.

Debe permitir identificar como mínimo:

- código/tipo del error;
- mensaje;
- timestamp;
- endpoint cuando resulte útil.

No exponer detalles internos.

---

# Status HTTP

Mapear cada error al código HTTP apropiado.

Una excepción de negocio no implica automáticamente HTTP 500.

Los errores inesperados deberán producir una respuesta genérica y registrar
internamente el detalle técnico.