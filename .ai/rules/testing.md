# Pruebas

## Filosofía

La cobertura de código no es un objetivo del proyecto.

Las pruebas deberán aportar valor real.

Priorizar pruebas sobre lógica que pueda provocar:

- pérdida de dinero;
- inconsistencias de inventario;
- problemas de autorización;
- corrupción de datos;
- regresiones importantes.

---

# Backend

Utilizar:

- JUnit 5
- Mockito
- herramientas de testing proporcionadas por Spring Boot

Utilizar pruebas unitarias cuando permitan probar lógica aisladamente.

Utilizar pruebas de integración cuando sea importante comprobar la interacción
real entre componentes.

---

# Estructura

Preferir:

Given

When

Then

Los comentarios pueden utilizarse para separar estas secciones cuando mejoren
la lectura.

---

# Nombres

Los nombres deben describir escenario y resultado.

Ejemplo:

```text
shouldRejectSaleWhenStockIsInsufficient
```

o la convención equivalente utilizada consistentemente en el proyecto.

No mezclar convenciones arbitrariamente dentro del mismo módulo.

---

# Controllers

Utilizar las herramientas de prueba MVC/Web disponibles en la versión de
Spring Boot configurada en el proyecto.

No levantar todo el contexto de Spring cuando una prueba más pequeña sea
suficiente.

---

# Integración

Utilizar `@SpringBootTest` cuando realmente sea necesario probar integración
con el contexto completo.

Para cambios de configuración o despliegue, añadir verificaciones proporcionales al
riesgo, por ejemplo:

- arranque del stack desde configuración de ejemplo;
- healthchecks y conexión real entre servicios;
- persistencia después de reiniciar contenedores;
- recarga directa de una ruta profunda de la SPA;
- ausencia de URLs locales y secretos en el bundle productivo.

Antes de una entrega o publicación importante, realizar una prueba de arranque desde
un checkout limpio. No convertir esta prueba costosa en requisito para cada cambio de
código si una verificación más pequeña es suficiente.

---

# Reglas

No utilizar reflexión para acceder a detalles privados únicamente para poder
probarlos.

Probar comportamiento observable.

No reproducir la lógica de producción dentro de la prueba.
