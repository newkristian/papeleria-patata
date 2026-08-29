# Descuento de producto por cantidad

**Estado:** Aprobado
**Última revisión:** 28 de agosto de 2026

## Objetivo

Aplicar automáticamente un porcentaje de descuento cuando la cantidad de un mismo
producto alcanza un umbral configurado.

## Regla de negocio

Cada regla debe contener:

- producto o categoría aplicable;
- cantidad mínima entera mayor que cero;
- porcentaje mayor que cero y menor que 100;
- vigencia y estado heredados de la promoción.

Ejemplo inicial:

| Producto | Cantidad | Resultado |
|---|---:|---:|
| Cuaderno profesional | 1 a 9 | Sin descuento |
| Cuaderno profesional | 10 o más | 5% de descuento |

Podrán configurarse varios escalones para el mismo producto. Si existen reglas con
umbrales de 10 unidades al 5% y 20 unidades al 8%, una compra de 25 unidades cumple
ambas condiciones; ambas se evalúan y se utiliza la de mayor beneficio conforme a
`PROMOCIONES_PRODUCTO.md`.

## Cálculo autorizado

Para cada línea, el backend calcula:

```text
precioListaUnitario = precioVenta del producto persistido
subtotalLista = precioListaUnitario × cantidad
descuentoMonto = subtotalLista × porcentaje / 100
subtotalFinal = subtotalLista - descuentoMonto
```

El request de venta no debe aceptar como autoridad el precio unitario, porcentaje,
monto descontado ni subtotal enviados por el frontend.

## Redondeo y precisión

- Los cálculos monetarios deben utilizar `BigDecimal`.
- Debe definirse una escala monetaria uniforme y un modo de redondeo explícito.
- El redondeo debe aplicarse de forma determinista y verificarse con pruebas.

## Cambios durante una venta

El backend vuelve a evaluar precio y promoción al confirmar la venta. Si una regla o
precio cambió desde que el frontend mostró el carrito, prevalece el cálculo vigente
del servidor y la respuesta debe permitir al frontend mostrar el total definitivo.

## Criterios de aceptación

- Una cantidad inferior al umbral no recibe descuento.
- Una cantidad igual o superior al umbral recibe el descuento configurado.
- Un atacante no puede obtener otro porcentaje modificando el payload.
- Repetir un producto en varias líneas no debe permitir evadir ni duplicar reglas;
  el backend debe consolidar o evaluar la cantidad total del producto.
- El resultado queda almacenado como fotografía histórica de la venta.

## Implementación verificada

La base financiera ya utiliza `BigDecimal` y el detalle de venta contiene los campos
históricos requeridos. Todavía no existe la evaluación de descuentos por cantidad.
