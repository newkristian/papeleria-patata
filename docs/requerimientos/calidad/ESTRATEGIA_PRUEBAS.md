# Estrategia de pruebas del MVP

**Estado:** En desarrollo  
**Última revisión:** 2 de septiembre de 2026

## Objetivo

Proteger la lógica con impacto financiero, de inventario y autorización mediante
pruebas unitarias y de integración enfocadas en riesgo.

## Implementación verificada

- La suite backend ejecuta 111 pruebas: 96 unitarias y quince escenarios de
  integración de ventas y catálogos.
- La integración usa Testcontainers con PostgreSQL 16.2 y aplica las 13 migraciones
  Flyway sobre un esquema limpio. La prueba de catálogos comprueba además V13 sobre
  los datos existentes de migraciones anteriores y confirma que una segunda ejecución
  de Flyway no duplica el proveedor reservado.
- Maven Surefire carga Mockito 5.20 como `javaagent`, evitando el self-attach no
  disponible en algunos JDK y entornos de CI.
- El frontend ejecuta 26 pruebas distribuidas en siete archivos.
- La prueba raíz de Angular verifica el `router-outlet` real y no un título eliminado.
- El 2 de septiembre de 2026 se verificaron satisfactoriamente la suite backend
  completa, la suite frontend y el build de producción Angular.
- La cobertura de catálogos incluye validación, conflictos de relaciones, matriz de
  roles, invariantes de `PENDIENTE`, reasignación masiva y rollback transaccional.
- La cobertura de productos incluye contratos de alta y edición, código de barras
  duplicado, asignación y cambio de proveedor, límites de campos, ciclo de vida
  lógico, persistencia de relaciones, bloqueo de venta inactiva y respuestas del POS
  sin costos.

## Consideraciones del entorno

- Testcontainers necesita acceso al socket de Docker. En entornos aislados la suite
  completa debe ejecutarse con ese acceso autorizado; las pruebas unitarias pueden
  ejecutarse excluyendo `VentaFlujoIntegrationTest` y `CatalogosIntegrationTest`.
- En el sandbox inspeccionado, esbuild termina en un deadlock incluso con un worker.
  El mismo `npm run build` finaliza correctamente fuera del sandbox, por lo que no se
  modificó configuración ni se actualizaron dependencias para ocultar una limitación
  del entorno.

## Pendientes conocidos

- Faltan pruebas específicas de autenticación y de algunos Controllers y Repositories
  fuera de los flujos integrales ya cubiertos.
- Las nuevas tareas de productos, inventario y fotografías deberán añadir
  pruebas orientadas a autorización, integridad, concurrencia y archivos hostiles.
