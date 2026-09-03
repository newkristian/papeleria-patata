# Administración de productos

**Estado:** En desarrollo  
**Última revisión:** 2 de septiembre de 2026

## Objetivo

Crear, consultar, actualizar, desactivar y reactivar productos con categoría,
proveedor, costos, precio y estado de inventario, sin eliminar su historial.

## Alcance aprobado

- La categoría continúa siendo obligatoria.
- La relación persistente con proveedor continúa siendo obligatoria. Cuando el
  proveedor comercial todavía no se conozca, el backend asignará el proveedor
  reservado `PENDIENTE`; el cliente nunca dependerá de su ID concreto.
- El usuario podrá asignar un proveedor real, cambiarlo o devolver el producto a
  `PENDIENTE` posteriormente.
- El alta permitirá indicar que la cantidad aún no fue contabilizada. En ese caso se
  aplicarán las reglas de `../inventario/CANTIDAD_DESCONOCIDA.md`.
- La eliminación funcional será exclusivamente lógica mediante `activo = false`.
- Un producto inactivo conservará ventas, movimientos, promociones y fotografías; no
  podrá venderse ni recibir movimientos, pero seguirá disponible en consultas
  administrativas y podrá reactivarse.
- El precio de venta continuará calculándose en backend a partir del costo y el
  porcentaje de ganancia, con la precisión definida por el proyecto.

## Autorización aprobada

- `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA`: crear y editar productos.
- `ADMINISTRADOR` y `GERENTE`: desactivar y reactivar productos.
- `ADMINISTRADOR`, `GERENTE` e `INVENTARISTA`: consultar el catálogo administrativo,
  incluidos productos inactivos y costos.
- `VENDEDOR`: consultar únicamente los datos necesarios de productos activos para el
  POS; no puede consultar costos ni ejecutar escrituras.
- La autorización debe aplicarse en backend. Los guards y controles visuales del
  frontend no sustituyen `@PreAuthorize` ni las validaciones del servicio.

## Validaciones aprobadas

- Código de barras obligatorio, normalizado, con longitud máxima y unicidad; un
  duplicado debe producir un conflicto controlado.
- Nombre obligatorio y longitudes máximas para nombre y descripción.
- Categoría existente y activa cuando corresponda.
- Proveedor existente y activo cuando sea distinto de `PENDIENTE`.
- Costo mayor que cero, porcentaje de ganancia no negativo y máximo dos decimales.
- Stock mínimo no negativo y unidad de medida obligatoria con longitud acotada.
- El backend no aceptará precio de venta, existencia ni relaciones fabricadas por el
  cliente fuera de los contratos expresamente definidos.

## Implementación verificada

- Existen contratos independientes para creación y reemplazo completo mediante
  actualización; stock, estado activo y precio calculado no son controlables desde
  esos contratos.
- El servicio calcula el porcentaje de ganancia, valida relaciones requeridas y
  asigna `PENDIENTE` cuando se omite el proveedor.
- Costo, porcentaje de ganancia y precio de venta utilizan `BigDecimal`; el precio se
  redondea a dos decimales con `HALF_UP`.
- El código de barras se normaliza a mayúsculas y su unicidad se valida sin distinguir
  mayúsculas; los duplicados producen 409.
- Existen operaciones explícitas para desactivar y reactivar. La desactivación no
  elimina la fila ni sus relaciones y un producto inactivo no puede venderse.
- El POS recibe un DTO de listado sin costos y solo puede consultar productos activos.
- La autorización de lectura y escritura está aplicada por operación en controller y,
  para reglas sensibles, también en servicio.

## Pendientes conocidos

- Añadir la interfaz administrativa para completar el flujo desde una sesión de
  usuario sin llamadas manuales a la API.
- Completar en la tarea de inventario la semántica de ventas y movimientos mientras
  `cantidadDesconocida = true`, incluida la concurrencia.
- Completar el pipeline seguro y asíncrono de fotografías.

## Dependencias de datos verificadas

### Relaciones obligatorias para crear el producto

- `Categoria`: obligatoria en contrato, JPA y PostgreSQL. Su CRUD backend existe y el
  mantenimiento frontend debe estar listo antes del formulario de productos.
- `Proveedor`: obligatorio en JPA y PostgreSQL. El backend asignará el registro
  reservado `PENDIENTE` cuando el usuario no seleccione uno.

### Relaciones posteriores que no bloquean el alta

- `ProductoFoto`: cada foto requiere un producto ya persistido, pero un producto puede
  crearse sin fotos.
- `InventarioMovimiento`: cada movimiento requiere producto y usuario, pero el alta
  puede completarse con cantidad desconocida sin crear un movimiento inicial.
- `DetalleVenta`: se crea al vender y referencia un producto existente; no participa
  en el alta.
- `Promocion` y `AutorizacionDescuento`: pueden referenciar el producto después de su
  creación; son opcionales para el catálogo.

## Dependencia futura no bloqueante

- El catálogo será global, pero cada tienda deberá tener existencias independientes y
  transferencias entre tiendas. Este requerimiento quedó diferido y se documenta en
  `../inventario/STOCK_POR_TIENDA.md`.
- Durante el alcance actual se conservará el modelo existente: todas las tiendas
  acceden temporalmente al mismo `stockActual`. Esta simplificación es consciente y no
  debe presentarse como el modelo definitivo.

## Criterios de aceptación

- El catálogo puede administrarse sin exponer entidades JPA como contrato HTTP.
- Puede crearse un producto sin proveedor definido y queda asociado a `PENDIENTE`.
- Un producto desactivado conserva todo su historial y no puede venderse.
- Un usuario sin rol autorizado no puede crear, editar, desactivar ni reactivar
  productos mediante una solicitud HTTP directa.
- Un código duplicado, una relación inexistente o un valor fuera de rango produce una
  respuesta HTTP consistente sin exponer detalles internos.
- Un `ADMINISTRADOR` o `INVENTARISTA` puede iniciar sesión, crear o seleccionar una
  categoría, omitir el proveedor cuando aún no se conoce y completar el alta sin
  manipular la base de datos ni llamar manualmente a la API.
