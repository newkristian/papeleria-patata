# Estrategia de pruebas del MVP

**Estado:** Implementado y verificado  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Proteger la lógica con impacto financiero, de inventario y autorización mediante
pruebas unitarias y de integración enfocadas en riesgo.

## Implementación verificada

- La suite backend ejecuta **134 pruebas automatizadas** (0 fallos, 0 errores):
  - Pruebas unitarias de servicios, repositorios, controladores, promociones,
    procesamiento de fotografías y validadores de seguridad.
  - Cuatro suites de integración E2E ejecutadas sobre PostgreSQL 16 limpio en
    Testcontainers:
    1. `VentaFlujoIntegrationTest`: ciclo de venta, promociones y descuentos.
    2. `CatalogosIntegrationTest`: categorías, productos y búsquedas combinadas.
    3. `SemanticaInventarioIntegrationTest`: inventario con cantidad desconocida,
       regularización y ventas sin bloqueo.
    4. `FlujoIntegralCargaProductosIntegrationTest`: ciclo de negocio completo
       (proveedor, producto, foto asíncrona, venta desconocida, conteo, venta controlada,
       desactivación lógica, reasignación automática a `PENDIENTE` y matriz de 4 roles).
- Se ejecutan las 14 migraciones Flyway (V1 a V14) sobre esquema limpio sin fallas.
- Maven Surefire carga Mockito 5.20 como `javaagent`.
- El frontend ejecuta **90 pruebas unitarias** en **20 suites Vitest** (100% aprobadas),
  cubriendo modelos, servicios HTTP, interceptores, guards de autorización,
  componentes administrativos (`CategoriasAdminComponent`, `ProveedoresAdminComponent`,
  `ProductosAdminComponent`, `InventarioAdminComponent`) y modales (`ProductoFotosModalComponent`).
- Compilación de producción Angular (`ng build`) completada con éxito, generando
  chunks lazy optimizados sin advertencias.
- Se verificó la matriz completa de roles (`ADMINISTRADOR`, `GERENTE`, `INVENTARISTA` y `VENDEDOR`)
  tanto a nivel de endpoints HTTP directos como en la experiencia de usuario en frontend.

## Consideraciones del entorno

- Testcontainers necesita acceso al socket de Docker.
- Se verificó que los contenedores locales de Docker y el servidor de desarrollo
  estén detenidos antes de correr suites completas para evitar colisiones de puertos.

## Cierre de pendientes del hito

- Pruebas de integración añadidas para todo el ciclo de vida de productos, fotos,
  proveedores, inventario y matriz de autorización.
- Pruebas frontend añadidas para todos los componentes y servicios del área administrativa.
