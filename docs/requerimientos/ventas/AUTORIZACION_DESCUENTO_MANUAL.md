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

## Contrato para el frontend (integración T7)

El flujo son **dos llamadas separadas**: emitir la autorización y, después, confirmar
la venta incluyendo su referencia. Ninguna otra llamada existe entre medio; el backend
no expone un endpoint para "consultar" o "cancelar" una autorización pendiente.

### 1. `carritoId`: generarlo una sola vez por operación de venta

El frontend genera un identificador (recomendado: UUID v4) **al iniciar el carrito**,
antes de pedir ninguna autorización, y lo reutiliza para:

- todas las solicitudes de autorización de esa venta (una por línea con descuento
  manual, si hay varias);
- el campo `carritoId` del `POST /api/v1/ventas` final.

Si el vendedor descarta el carrito sin confirmar la venta, el `carritoId` simplemente
se descarta; no hay nada que liberar en el backend (las autorizaciones no consumidas
expiran solas a los 2 minutos).

### 2. Solicitar la autorización

```
POST /api/v1/autorizaciones-descuento
Authorization: Bearer <JWT del vendedor>
Content-Type: application/json

{
  "username": "gerente@pos.com",
  "password": "<contraseña del gerente/administrador>",
  "productoId": 1,
  "cantidad": 2,
  "porcentaje": 20.00,
  "motivo": "Cliente frecuente, aprobado en piso de venta",
  "carritoId": "b3f1c2a0-....-uuid-del-carrito"
}
```

`cantidad` y `porcentaje` deben ser exactamente los que se van a vender: si el
vendedor cambia la cantidad o el porcentaje después de este paso, la autorización ya
no sirve (ver sección 4).

Respuesta `201 Created`:

```json
{
  "referencia": "VMqLCR3j--m1aP3tUuNRxpBNUp0WYN3SpqCHGROiMls",
  "expiraEn": "2026-09-01T23:55:32.920404136",
  "porcentaje": 20.00,
  "montoDescuentoEstimado": 15.00,
  "precioFinalEstimado": 30.00,
  "promocionAutomaticaDisponible": false,
  "montoPromocionAutomatica": 0.00,
  "diferenciaVsPromocionAutomatica": 15.00
}
```

- `referencia`: el token opaco. **Se entrega una sola vez**; el backend solo guarda su
  hash y no hay forma de recuperarlo si se pierde (habría que emitir una nueva
  autorización).
- `expiraEn`: fecha/hora ISO en la que deja de ser válida (emitida con 2 minutos de
  vigencia). Úsese para mostrar una cuenta regresiva y para descartar la referencia en
  el cliente sin esperar el rechazo del backend.
- `montoDescuentoEstimado` / `precioFinalEstimado`: informativos, calculados con el
  precio de catálogo en el momento de la solicitud. El backend vuelve a calcularlos al
  confirmar la venta; si el precio del producto cambió mientras tanto, estos valores
  pueden no coincidir con el detalle final (poco probable dentro de una ventana de 2
  minutos, pero posible).
- `promocionAutomaticaDisponible`, `montoPromocionAutomatica`,
  `diferenciaVsPromocionAutomatica`: para que el autorizador vea, antes de aprobar, si
  ya existía una promoción automática y cuánto beneficio adicional (o menor) implica
  el descuento manual. Esta comparación **no considera promociones de cliente** (el
  backend aún no conoce el cliente final de la venta en este paso); es una limitación
  conocida, no un error.

**Manejo de contraseña**: el campo `password` de este formulario nunca debe
guardarse en estado durable (`localStorage`, store persistente, etc.). Debe limpiarse
del estado del componente inmediatamente después de recibir la respuesta, sea éxito o
error, y nunca debe registrarse en logs del cliente.

### 3. Confirmar la venta con el descuento manual

En `POST /api/v1/ventas`, la línea del producto autorizado incluye la referencia, y la
venta completa incluye el `carritoId` usado al solicitarla:

```json
{
  "clienteId": null,
  "metodoPago": "EFECTIVO",
  "carritoId": "b3f1c2a0-....-uuid-del-carrito",
  "detalles": [
    { "productoId": 1, "cantidad": 2, "autorizacionDescuento": "VMqLCR3j--m1aP3tUuNRxpBNUp0WYN3SpqCHGROiMls" }
  ]
}
```

Reglas a respetar desde el frontend:

- `carritoId` es obligatorio en la venta si **cualquier** línea trae
  `autorizacionDescuento`; si ninguna línea lo trae, puede omitirse.
- La `cantidad` de la línea debe coincidir exactamente con la `cantidad` autorizada.
  Si el producto aparece repetido en varias líneas del carrito, el backend las
  consolida sumando cantidades antes de comparar: la referencia debe ir en una sola de
  esas líneas y la suma de todas las cantidades de ese producto debe ser igual a la
  cantidad autorizada. Poner referencias distintas en dos líneas del mismo producto se
  rechaza (ambigüedad) — no hacerlo desde el frontend, agrupar la línea en el carrito
  antes de solicitar la autorización.
- Cuando una línea trae `autorizacionDescuento`, el backend **no** evalúa ninguna
  promoción automática para esa línea: el descuento manual la reemplaza por completo.
  El frontend no debe mostrar ni sumar un descuento automático adicional para esa
  línea.
- La respuesta de la venta (`DetalleVentaResponseDTO`) trae `tipoDescuento: "MANUAL"`,
  `autorizadoPorUsuarioId` y `autorizacionDescuentoId` en la línea afectada, útiles
  para el recibo/ticket.

### 4. Qué invalida una autorización ya emitida

Cualquiera de estos casos hace que `POST /api/v1/ventas` rechace la referencia con
`400` y el mensaje genérico *"La autorización de descuento manual es inválida, expiró
o ya fue utilizada"* (deliberadamente genérico; el backend no distingue entre expirada,
ya usada o alterada en el mensaje, para no dar pistas a un intento de manipulación):

- pasaron más de 2 minutos desde la emisión;
- ya se usó en una venta (la propia venta que la consumió, o un intento anterior que
  falló *después* de consumirla — no ocurre en este backend porque el consumo vive en
  la misma transacción que la venta, pero sí puede ocurrir si el usuario reintenta tras
  un error de red habiendo la venta anterior sí terminado de guardarse);
- cambió el producto, la cantidad, el vendedor, la tienda o el `carritoId` respecto a
  lo autorizado;
- (caso raro) el costo del producto subió entre la emisión y el consumo al punto de
  volver a violar el piso de costo de un `GERENTE`.

Ante este `400`, el frontend debe **descartar la referencia guardada y volver a pedir
autorización** (no reintentar la venta con la misma referencia). No hay forma de
"refrescar" una autorización expirada; siempre es una nueva solicitud.

### 5. Otros códigos de error a manejar en el modal de autorización

- `401` — credenciales del autorizador incorrectas o cuenta inactiva (mensaje
  genérico, no distingue cuál de las dos cosas fue). Limpiar el campo de contraseña y
  dejar reintentar.
- `403` — rol no autorizado (ni `GERENTE` ni `ADMINISTRADOR`), `GERENTE` de otra
  tienda, o límite de intentos fallidos alcanzado (mensaje explícito de "demasiados
  intentos"; en ese caso no reintentar de inmediato, el backend bloquea por 15
  minutos).
- `400` — validación de campos (porcentaje fuera de 0–30, motivo vacío, etc.) o regla
  de negocio (`GERENTE` autorizando bajo costo, producto inactivo).

## Requerimientos relacionados

- `REGISTRAR_VENTA.md`
- `PROMOCIONES_PRODUCTO.md`
