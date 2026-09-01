# Promociones de producto

**Estado:** Implementado
**Última revisión:** 1 de septiembre de 2026

## Objetivo

Aplicar promociones automáticas a productos o categorías mediante un modelo que
permita incorporar nuevos tipos de promoción sin confiar cálculos financieros al
frontend.

## Alcance aprobado

Una promoción debe definir como mínimo:

- nombre y descripción;
- tipo de promoción;
- estado activo o inactivo;
- fecha y hora de inicio y fin opcionales;
- productos o categorías a los que aplica;
- prioridad para resolver empates;
- configuración específica correspondiente a su tipo.

La primera implementación incluirá el tipo `DESCUENTO_POR_CANTIDAD`. El diseño debe
permitir agregar posteriormente tipos como:

- `DOS_POR_UNO`;
- `PRECIO_ESPECIAL`;
- `PORCENTAJE_TEMPORAL`.

Las reglas específicas no deben almacenarse como código ejecutable ni expresiones
arbitrarias proporcionadas por usuarios.

## Evaluación

El backend debe:

1. obtener el precio de lista vigente desde el producto persistido;
2. localizar promociones activas, vigentes y aplicables;
3. validar las condiciones de cada promoción según su tipo;
4. calcular el beneficio monetario de cada candidata;
5. elegir únicamente la candidata que produzca el mayor beneficio para el cliente;
6. usar prioridad y después ID como desempate determinista;
7. conservar en el detalle de venta una fotografía de la promoción aplicada.

## Política de combinación

- Las promociones no se acumulan entre sí en la primera versión.
- Una promoción de cliente, como VIP, participa como otra candidata.
- Se utiliza una sola promoción automática por línea de venta.
- Un descuento manual autorizado reemplaza cualquier promoción automática de esa
  línea.
- Permitir combinaciones en el futuro requerirá una política explícita y un nuevo
  análisis financiero y de seguridad.

## Autoridad del backend

El frontend puede mostrar estimaciones, pero no decide:

- precio de lista;
- promoción aplicable;
- porcentaje o monto efectivo;
- subtotal ni total definitivo.

El backend debe recalcular la venta completa al confirmar el cobro. Cualquier precio,
promoción, descuento o total adicional enviado por el cliente debe ignorarse o
rechazarse según el contrato publicado.

## Persistencia histórica

Cada detalle de venta debe conservar al menos:

- precio de lista unitario;
- tipo e identificador de promoción aplicada;
- porcentaje o beneficio aplicado, cuando corresponda;
- monto descontado;
- precio unitario o subtotal final;
- cantidad vendida.

Modificar o desactivar una promoción no debe alterar ventas históricas.

## Seguridad y autorización

- Solo `ADMINISTRADOR` podrá administrar promociones en la primera versión.
- Las fechas, cantidades y beneficios deben validarse en backend.
- Una promoción nunca puede producir totales negativos ni unidades negativas.
- Deben rechazarse valores no finitos, porcentajes fuera de rango y configuraciones
  incompletas.
- La administración y aplicación de promociones debe quedar disponible para
  auditoría.

## Criterios de aceptación

- Una venta produce el mismo total aunque el cliente HTTP intente modificar precios
  o descuentos.
- Entre varias promociones aplicables se selecciona exactamente una de forma
  determinista.
- Una promoción fuera de vigencia o inactiva nunca se aplica.
- Las ventas anteriores conservan los importes originalmente cobrados.

## Implementación verificada

- Modelo persistente (`Promocion`, `ReglaDescuentoPorCantidad`) y administración REST
  restringida a `ADMINISTRADOR` (`PromocionController`).
- `MotorPromocionesService` evalúa candidatas de producto/categoría y de cliente
  (incluida VIP) para una cantidad ya consolidada y elige una sola ganadora: mayor
  beneficio, luego prioridad explícita, luego vigencia con fecha de inicio y fin
  definidas sobre vigencia atemporal, y finalmente la promoción más reciente (mayor
  ID) como desempate final.
- `VentaService.crearVenta` consolida productos repetidos por `productoId` antes de
  evaluar, invoca el motor por cada línea y aplica como máximo una promoción por
  producto; el frontend nunca decide precio, promoción, porcentaje ni total.
- Cada `DetalleVenta` conserva precio de lista, tipo y monto de descuento, porcentaje,
  precio final, subtotal y la referencia a la promoción ganadora (de producto o de
  cliente, mutuamente excluyentes). Modificar o desactivar una promoción no altera
  ventas históricas (verificado manualmente).
- Un fallo en cualquier línea revierte la venta completa antes de persistir.

Pendiente: autorización manual de descuento (Tarea 6) reemplazará la promoción
automática de una línea cuando se implemente.
