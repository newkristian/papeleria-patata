# Estrategia de pruebas del MVP

**Estado:** En desarrollo  
**Última revisión:** 2 de septiembre de 2026

## Objetivo

Proteger la lógica con impacto financiero, de inventario y autorización mediante
pruebas unitarias y de integración enfocadas en riesgo.

## Implementación verificada

- La suite backend ejecuta 78 pruebas: 72 unitarias y seis escenarios de integración
  del flujo de venta.
- La integración usa Testcontainers con PostgreSQL 16.2 y aplica las 12 migraciones
  Flyway sobre un esquema limpio.
- Maven Surefire carga Mockito 5.20 como `javaagent`, evitando el self-attach no
  disponible en algunos JDK y entornos de CI.
- El frontend ejecuta 26 pruebas distribuidas en siete archivos.
- La prueba raíz de Angular verifica el `router-outlet` real y no un título eliminado.
- El 2 de septiembre de 2026 se verificaron satisfactoriamente la suite backend
  completa, la suite frontend y el build de producción Angular.

## Consideraciones del entorno

- Testcontainers necesita acceso al socket de Docker. En entornos aislados la suite
  completa debe ejecutarse con ese acceso autorizado; las pruebas unitarias pueden
  ejecutarse excluyendo `VentaFlujoIntegrationTest`.
- En el sandbox inspeccionado, esbuild termina en un deadlock incluso con un worker.
  El mismo `npm run build` finaliza correctamente fuera del sandbox, por lo que no se
  modificó configuración ni se actualizaron dependencias para ocultar una limitación
  del entorno.

## Pendientes conocidos

- Faltan pruebas específicas de autenticación, Controllers y Repositories fuera del
  flujo integral ya cubierto.
- Las nuevas tareas de productos, proveedores, inventario y fotografías deberán añadir
  pruebas orientadas a autorización, integridad, concurrencia y archivos hostiles.
