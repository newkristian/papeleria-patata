# Desarrollo Backend Primero

## Regla fundamental

No deberá implementarse en frontend ninguna funcionalidad cuyo soporte backend
no esté completo.

El backend es una dependencia funcional obligatoria del frontend.

---

# Ejemplo

Antes de desarrollar una pantalla de productos deberán existir y estar
funcionales los endpoints necesarios para dicha pantalla.

Por ejemplo:

GET /products
GET /products/{id}
POST /products
PUT /products/{id}
DELETE /products/{id}

cuando el alcance definido requiera un CRUD completo.

---

# Backend completo

Se considera que el backend necesario está completo cuando:

- existen los endpoints requeridos;
- request y response contienen los datos necesarios;
- existen validaciones;
- existen reglas de negocio necesarias;
- la autorización está implementada;
- los errores esperados están controlados;
- la persistencia necesaria está implementada;
- los endpoints pueden consumirse correctamente.

No es suficiente que exista únicamente el Controller.

---

# Inicio de frontend

Antes de comenzar una tarea de frontend, el agente deberá verificar:

1. qué endpoints necesita;
2. si existen;
3. si los contratos actuales contienen toda la información requerida;
4. si los códigos de respuesta son adecuados;
5. si los errores esperados pueden manejarse correctamente.

Solo después de esta verificación podrá comenzar el frontend.

---

# Información faltante

Si durante la implementación frontend se descubre que falta información en el
backend, deberá detenerse el desarrollo frontend.

Ejemplos:

- falta un atributo en response;
- falta un parámetro de búsqueda;
- falta paginación;
- falta un endpoint;
- falta una validación;
- falta información relacionada;
- el contrato no permite completar correctamente la pantalla.

---

# Procedimiento obligatorio

Cuando ocurra:

Frontend

↓

Se detecta dependencia faltante

↓

DETENER frontend

↓

Analizar cambio backend

↓

Agregar tarea backend al plan

↓

Implementar backend

↓

Revisar y aprobar backend

↓

Continuar frontend

---

# Prohibiciones

No utilizar datos ficticios para evitar modificar backend salvo que se trate
explícitamente de un prototipo solicitado por el propietario.

No duplicar reglas de negocio en frontend para compensar funcionalidades
faltantes en backend.

No construir contratos temporales únicamente para avanzar visualmente.

No asumir campos que todavía no existen en la API.

---

# Regla de dominio

El frontend representa y consume el comportamiento del sistema.

No define las reglas de negocio del sistema.