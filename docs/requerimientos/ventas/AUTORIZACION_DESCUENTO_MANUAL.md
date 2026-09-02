# Autorización de descuento manual

**Estado:** Implementado (backend)
**Última revisión:** 1 de septiembre de 2026

## Objetivo

Permitir que un `GERENTE` o `ADMINISTRADOR` autorice excepcionalmente un descuento
distinto al calculado automáticamente para una línea de producto.

## Alcance aprobado

- El descuento manual se aplica por producto, no sobre toda la venta.
- El porcentaje máximo manual es 30%.
- `GERENTE` puede autorizar hasta 30% siempre que el precio final no sea inferior al
  costo de compra vigente.
- `ADMINISTRADOR` puede autorizar hasta 30% aunque el precio final quede por debajo
  del costo.
- Por ahora la autorización requiere usuario y contraseña; no se implementará NIP.
- Debe capturarse un motivo obligatorio.

Ni `GERENTE` ni `ADMINISTRADOR` pueden autorizar un descuento superior al 30% mediante
este flujo.

## Flujo de autorización

1. El vendedor selecciona una línea y solicita cambiar el descuento.
2. El frontend abre un modal y solicita usuario, contraseña, porcentaje y motivo.
3. Las credenciales se envían exclusivamente mediante HTTPS a un endpoint dedicado.
4. El backend autentica nuevamente al autorizador y verifica que esté activo.
5. El backend valida rol, tienda, producto, cantidad, porcentaje, costo y motivo.
6. Si procede, emite una autorización opaca, temporal y de un solo uso.
7. La venta referencia esa autorización al confirmar el cobro.
8. El backend vuelve a validar el contexto, calcula el importe y consume la
   autorización dentro de la misma transacción de la venta.

La contraseña no debe incluirse en el request final de venta ni conservarse en el
frontend después de cerrar el modal.

## Alcance de la autorización temporal

La autorización debe quedar vinculada a:

- autorizador;
- vendedor solicitante;
- tienda;
- producto;
- cantidad;
- porcentaje autorizado;
- motivo;
- identificador del carrito u operación;
- fecha de emisión y expiración.

Vigencia inicial recomendada: dos minutos. Debe ser imposible reutilizarla en otra
venta, producto, cantidad, tienda o porcentaje.

## Política frente a promociones

- El descuento manual reemplaza la promoción automática de la línea.
- No se suma a descuentos por cantidad, promociones VIP ni futuras promociones.
- El backend debe mostrar o devolver la diferencia entre la promoción automática y
  el descuento manual para que la decisión sea informada.

## Validaciones obligatorias

- Porcentaje numérico, finito, mayor o igual a 0 y menor o igual a 30.
- Motivo no vacío y con longitud limitada.
- Credenciales válidas y usuario activo con rol permitido.
- `GERENTE` asignado a la misma tienda que la venta.
- Precio y costo obtenidos exclusivamente desde persistencia.
- Una autorización expirada, consumida o alterada debe rechazarse.
- Intentos fallidos repetidos deben limitarse y registrarse sin guardar contraseñas.

## Auditoría

Debe conservarse:

- venta y detalle afectados;
- vendedor;
- autorizador;
- rol del autorizador;
- precio de lista y costo considerados;
- promoción automática disponible;
- porcentaje y monto manual aplicados;
- motivo;
- fecha y tienda.

Nunca deben registrarse contraseña, tokens completos ni datos secretos.

## Criterios de aceptación

- Un `VENDEDOR` no puede elegir descuentos manuales sin autorización válida.
- Un `GERENTE` no puede vender por debajo del costo.
- Un `ADMINISTRADOR` puede vender por debajo del costo, pero nunca superar 30%.
- Modificar el porcentaje o producto después de autorizar invalida la operación.
- Una autorización no puede utilizarse más de una vez.
- El frontend no puede simular una autorización construyendo manualmente el payload.

## Implementación verificada

- `POST /api/v1/autorizaciones-descuento` reautentica al gerente/administrador con
  usuario y contraseña (mismo `AuthenticationManager` que `/auth/login`), valida rol,
  usuario activo, tienda del gerente y el piso de costo (`GERENTE` no puede quedar
  bajo costo; `ADMINISTRADOR` sí, sin superar 30%).
- Emite una autorización opaca de 2 minutos; solo se persiste el hash SHA-256 del
  token, nunca el token en claro.
- `VentaService.crearVenta` consume la autorización dentro de la misma transacción de
  la venta, revalidando producto, cantidad, vendedor, tienda y carrito, y consumiéndola
  de forma atómica (`UPDATE ... WHERE consumida = false AND vigente`) para que dos
  consumos concurrentes no acepten el mismo permiso.
- El descuento manual reemplaza la promoción automática de la línea; nunca se
  acumulan. La respuesta de la solicitud incluye la diferencia frente a la mejor
  promoción automática de producto/categoría disponible (sin considerar cliente, por
  ahora).
- Límite de 5 intentos fallidos por vendedor en 15 minutos (en memoria, documentado
  como limitación de instancia única).
- Se corrigió un defecto preexistente en `GlobalExceptionHandler`: credenciales
  inválidas devolvían 500 en vez de 401, afectando también a `/auth/login`.

Pendiente: interfaz de usuario del modal de autorización y su manejo de expiración,
rechazo y limpieza de estado sensible (Tarea 7).
