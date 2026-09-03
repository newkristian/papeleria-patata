# Seguridad

La seguridad tiene prioridad máxima según `.ai/core/priorities.md`.

---

# Autenticación

El sistema utiliza autenticación basada en JWT.

Los tokens deberán:

- tener expiración;
- ser firmados utilizando algoritmos seguros;
- ser validados completamente en backend.

Nunca aceptar un JWT únicamente porque pueda decodificarse.

Verificar como mínimo:

- firma;
- expiración;
- claims requeridos.

---

# Autorización

Autenticación y autorización son responsabilidades diferentes.

Un usuario autenticado no obtiene automáticamente acceso a todos los recursos.

La autorización deberá realizarse en backend.

Preferir autorización cerca de la capa de aplicación mediante mecanismos como:

```java
@PreAuthorize(...)
```

cuando resulte apropiado.

No depender exclusivamente del Controller para proteger reglas críticas de
negocio.

---

# Control de acceso a recursos

Verificar que el usuario tenga autorización sobre el recurso solicitado.

Nunca asumir que conocer un ID implica autorización para acceder al recurso.

Prevenir vulnerabilidades IDOR/BOLA.

---

# Validación

Toda entrada externa debe considerarse no confiable.

Validar:

- body;
- parámetros;
- path variables;
- archivos;
- datos provenientes de sistemas externos.

---

# SQL

Utilizar parámetros mediante JPA o consultas parametrizadas.

Nunca construir SQL concatenando datos proporcionados por usuarios.

---

# XSS

No insertar contenido proporcionado por usuarios como HTML sin sanitización.

Evitar mecanismos que permitan renderizar HTML arbitrario en Angular.

---

# CORS

Configurar explícitamente los orígenes permitidos.

No utilizar:

Access-Control-Allow-Origin: *

en producción cuando existan endpoints autenticados.

---

# Secretos

Nunca almacenar en el repositorio:

- contraseñas;
- claves JWT;
- tokens;
- credenciales de base de datos;
- API keys.

Utilizar variables de entorno o mecanismos equivalentes.

En producción, la ausencia de un secreto o credencial obligatoria deberá impedir el
arranque. Nunca utilizar un secreto de demostración como valor de respaldo.

Todo valor incluido en el bundle Angular es público, incluso si proviene de un
archivo `environment`. Solo deben colocarse allí URLs, flags y claves expresamente
públicas.

Eliminar un secreto del estado actual del repositorio no lo elimina del historial.
Si un secreto fue versionado, deberá rotarse inmediatamente y después evaluarse la
limpieza del historial con autorización del propietario.

La exposición de Swagger UI y de la especificación OpenAPI deberá definirse por
ambiente. En producción deberán deshabilitarse o protegerse, salvo que publicar la
documentación sea una decisión explícita del producto.

---

# Logs

Nunca registrar:

- contraseñas;
- JWT completos;
- secretos;
- números completos de tarjetas;
- datos sensibles innecesarios.

---

# Errores

Las respuestas HTTP no deberán exponer:

- stack traces;
- SQL;
- rutas internas;
- secretos;
- detalles de infraestructura.

---

# Dependencias

No agregar dependencias conocidas como vulnerables.

Una actualización necesaria por una vulnerabilidad crítica tiene prioridad
sobre las restricciones normales de actualización de dependencias.
