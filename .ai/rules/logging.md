# Logging

## Implementación

Utilizar SLF4J.

Con Lombok preferir:

```java
@Slf4j
```

---

# Niveles

ERROR:
fallos que requieren atención.

WARN:
situaciones anómalas recuperables.

INFO:
eventos relevantes del negocio o ciclo de vida.

DEBUG:
información técnica útil para diagnóstico.

No utilizar ERROR para eventos esperados.

---

# Formato

Utilizar placeholders:

```java
log.info("Venta registrada. saleId={}, userId={}", saleId, userId);
```

No concatenar cadenas:

```java
log.info("Venta registrada " + saleId);
```

---

# Contexto

Incluir identificadores relevantes cuando ayuden al diagnóstico.

Ejemplos:

- saleId
- userId
- requestId

Evitar registrar objetos completos indiscriminadamente.

---

# Datos sensibles

Nunca registrar secretos o credenciales.

Consultar `security.md` para las restricciones completas.

---

# Excepciones

Cuando sea necesario diagnosticar una excepción inesperada, registrar también
el throwable:

```java
log.error("Error procesando venta. saleId={}", saleId, exception);
```

No registrar el mismo error repetidamente en múltiples capas salvo que cada log
aporte información diferente.