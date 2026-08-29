# Cobro desde el POS

**Estado:** Aprobado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Confirmar una venta, seleccionar el método de pago y mostrar el resultado al cajero.

## Alcance aprobado

- Cobro con el método único admitido actualmente por la API.
- Cálculo de cambio para efectivo y prevención de envíos duplicados.
- Manejo visible de errores de stock, autorización y conexión.
- Presentación de promociones calculadas por el backend para cada producto.
- Botón por línea para solicitar un descuento manual.
- Modal de reautenticación mediante usuario y contraseña de `GERENTE` o
  `ADMINISTRADOR`, porcentaje y motivo.

## Implementación verificada

No existe integración frontend con `POST /api/v1/ventas`.

## Dependencias pendientes

- Corregir autenticación y validación de precios en backend antes de integrar.
- Implementar promociones y autorizaciones de descuento en backend antes del modal.

## Seguridad del modal de autorización

- No almacenar ni autocompletar la contraseña del autorizador.
- Limpiar credenciales y estado sensible al cerrar o completar el modal.
- No calcular ni conceder permisos localmente.
- Utilizar únicamente la autorización temporal emitida por el backend.
- Mostrar el total definitivo devuelto por el servidor.
