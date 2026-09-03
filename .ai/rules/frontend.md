# Frontend

## Stack

- Angular 22
- TypeScript
- Tailwind CSS

Utilizar APIs modernas de Angular.

---

# Componentes

Utilizar Standalone Components.

No crear nuevos NgModules salvo que una dependencia externa lo requiera.

Los componentes deben mantener responsabilidades pequeñas y claras.

Dividir un componente cuando mezcle responsabilidades independientes.

---

# Estado

Utilizar Signals como mecanismo principal para estado local y derivado.

Preferir:

- `signal()`
- `computed()`
- `input()`
- `output()`

Utilizar `effect()` únicamente para efectos secundarios reales.

No utilizar `effect()` para propagar estado cuando `computed()` pueda
representarlo declarativamente.

---

# RxJS

RxJS continúa siendo válido para flujos asíncronos.

Utilizarlo especialmente para:

- HttpClient
- streams asíncronos
- composición de eventos
- operaciones donde Observable sea naturalmente apropiado

No convertir todo Observable a Signal automáticamente.

No utilizar `Subject` o `BehaviorSubject` como mecanismo de estado por defecto.

---

# Templates

Utilizar el control de flujo moderno:

- `@if`
- `@for`
- `@switch`

Evitar lógica compleja dentro del template.

Los cálculos derivados deben realizarse mediante Signals, propiedades o
funciones apropiadas.

---

# Comunicación HTTP

Centralizar el acceso HTTP en servicios responsables de comunicación con API.

Los componentes no deberán construir URLs ni conocer detalles de endpoints.

La URL base de la API y los flags dependientes del ambiente deberán centralizarse
en la configuración de Angular. Los servicios conocerán únicamente la ruta de su
recurso y no deberán repetir hosts o puertos.

Cuando frontend y backend se publiquen bajo el mismo origen, preferir una ruta
relativa atendida por el reverse proxy. Una URL absoluta requiere una configuración
CORS explícita para el origen real.

Verificar en el build de producción que no permanezcan referencias accidentales a
`localhost`, puertos locales ni endpoints de desarrollo.

La configuración de Angular forma parte del bundle público. Nunca colocar en
`environment.ts`, archivos de configuración en runtime ni constantes TypeScript:

- contraseñas;
- secretos JWT;
- tokens administrativos;
- claves privadas o credenciales de servicios.

Utilizar configuración en runtime únicamente cuando exista la necesidad concreta de
promover una misma imagen entre ambientes sin recompilarla. No agregar ese mecanismo
por anticipado.

---

# Tailwind CSS

Utilizar Tailwind como mecanismo principal de estilos.

Favorecer composición mediante utilidades.

Extraer componentes cuando grupos complejos de estilos se repitan junto con
una misma responsabilidad visual.

Evitar CSS personalizado cuando Tailwind resuelva claramente el caso.

---

# Estado global

No introducir NgRx u otra librería de estado global sin una necesidad
demostrable.

Comenzar con Signals y servicios especializados.

La complejidad del manejo de estado deberá crecer únicamente cuando el proyecto
lo requiera.

---

# Seguridad

Nunca considerar el frontend una frontera de seguridad.

Ocultar botones o rutas no sustituye la autorización del backend.

Toda autorización real deberá verificarse en el servidor.
