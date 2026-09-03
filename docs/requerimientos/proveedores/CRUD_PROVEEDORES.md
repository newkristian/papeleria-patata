# Administración de proveedores

**Estado:** En desarrollo  
**Última revisión:** 2 de septiembre de 2026

## Objetivo

Crear, consultar, actualizar y desactivar proveedores asociados al catálogo sin
romper productos ni referencias históricas.

## Alcance aprobado

- Existirá exactamente un proveedor reservado con nombre `PENDIENTE` para productos
  cuyo proveedor comercial aún no se conozca.
- La aplicación resolverá este proveedor mediante una identidad protegida y nunca
  asumirá un ID generado concreto.
- `PENDIENTE` no podrá renombrarse, desactivarse, recibir pagos ni utilizarse como un
  proveedor comercial ordinario.
- La eliminación será lógica. Antes de desactivar un proveedor, todos sus productos
  se reasignarán a `PENDIENTE` dentro de la misma transacción.
- Un fallo durante la reasignación deberá revertir también la desactivación.
- Los selectores ordinarios mostrarán proveedores activos y excluirán `PENDIENTE`,
  salvo cuando deban representar el valor actual de un producto.
- La consulta administrativa admitirá búsqueda, paginación y filtro por estado.

## Autorización aprobada

- `ADMINISTRADOR`: consultar, crear, editar y desactivar proveedores.
- `GERENTE`: consultar, crear y editar proveedores; no puede desactivarlos.
- `INVENTARISTA`: consultar proveedores activos para asociarlos a productos; no puede
  crear, editar ni desactivar.
- `VENDEDOR`: no puede utilizar el mantenimiento de proveedores.
- Toda escritura debe protegerse en backend con autorización explícita.

## Validaciones aprobadas

- Nombre obligatorio, normalizado y con longitud máxima.
- RFC opcional con longitud y formato razonables; teléfono, email y contacto opcionales
  con longitudes acotadas y email válido cuando se proporcione.
- Porcentaje de comisión entre cero y cien, con máximo dos decimales.
- Los errores de validación, ausencia y conflicto deben utilizar respuestas HTTP
  consistentes y no excepciones genéricas.

## Implementación verificada

- La migración V13 crea exactamente una identidad de sistema `PENDIENTE`, agrega el
  estado activo y protege en PostgreSQL su modificación, eliminación y uso en pagos.
- Controller, Service, Repository, Mapper y DTOs implementan consulta, creación,
  edición, búsqueda paginada por término/estado y desactivación lógica.
- La desactivación reasigna primero todos los productos a `PENDIENTE` en la misma
  transacción. Un fallo posterior revierte también la reasignación.
- El listado para selección devuelve únicamente proveedores comerciales activos; la
  búsqueda administrativa incluye el filtro de estado y nunca expone `PENDIENTE` como
  opción comercial.
- Bean Validation, errores 400/404/409 y la matriz de autorización se verificaron con
  pruebas unitarias y solicitudes HTTP reales para los cuatro roles.
- El porcentaje de comisión utiliza `BigDecimal` y `NUMERIC(5, 2)`, con rango de
  cero a cien protegido en el contrato y en la base de datos.

## Pendientes conocidos

- Hacer que el contrato de productos asigne automáticamente `PENDIENTE` cuando el
  cliente omita proveedor; corresponde a la Tarea 4 del plan de carga de productos.
- Implementar la interfaz administrativa.

## Dependencia no bloqueante

- `REPORTE_COMISIONES_PROVEEDOR.md` debe definir si un reporte histórico agrupa por el
  proveedor actual del producto o por una fotografía del proveedor al momento de la
  venta. La reasignación a `PENDIENTE` puede cambiar agrupaciones basadas en la
  relación actual. Esta decisión no bloquea el mantenimiento, porque el reporte aún
  se encuentra incompleto, pero deberá resolverse antes de terminar dicho reporte.

## Criterios de aceptación

- Crear un producto sin proveedor seleccionado lo asocia a `PENDIENTE` sin relajar la
  clave foránea de productos.
- Desactivar un proveedor reasigna todos sus productos y no produce 409 por esa
  relación.
- `PENDIENTE` permanece único y protegido.
- Un rol no autorizado recibe 403 aunque construya manualmente la solicitud.
- Los datos inválidos reciben 400 y un proveedor inexistente recibe 404 sin detalles
  internos.
