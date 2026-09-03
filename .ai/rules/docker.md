# Docker y Despliegue

## Entorno

El sistema se despliega mediante Docker Compose.

Los cambios deben mantener compatible este mecanismo de despliegue salvo
autorización explícita.

---

# Contenedores

Cada servicio deberá tener una responsabilidad clara.

Evitar instalar herramientas innecesarias dentro de imágenes de producción.

Preferir imágenes oficiales o ampliamente mantenidas.

Utilizar builds multietapa para separar herramientas de compilación del runtime
cuando reduzcan de forma clara el tamaño o la superficie de ataque de la imagen.

La imagen final deberá contener únicamente los artefactos y recursos necesarios para
ejecutar el servicio. Preferir procesos sin privilegios de `root` y otorgar escritura
solo a los directorios que realmente la necesiten.

Mantener un `.dockerignore` por contexto de build para excluir artefactos locales,
dependencias instaladas, logs, metadatos del IDE, secretos y otros archivos que no
formen parte de la imagen.

---

# Versiones

Evitar `latest`.

Utilizar versiones explícitas cuando sea razonable.

Los builds deberán respetar los archivos de bloqueo y wrappers versionados. En
Angular, preferir `npm ci` cuando exista `package-lock.json`; en backend, preferir el
Maven Wrapper del proyecto.

---

# Secretos

No incluir secretos dentro de:

- Dockerfile
- docker-compose.yml
- imágenes
- repositorio Git

Utilizar variables de entorno o mecanismos de secretos disponibles en el
entorno.

---

# Puertos

No publicar puertos que no necesiten ser accesibles desde el host.

Los servicios deberán comunicarse mediante las redes internas de Docker cuando
sea posible.

Dentro de Compose, utilizar el nombre del servicio como host. `localhost` dentro de
un contenedor identifica al propio contenedor y no a otro servicio ni al host.

---

# PostgreSQL

Mantener persistencia mediante volúmenes.

La eliminación o modificación de volúmenes persistentes requiere especial
precaución.

Nunca ejecutar operaciones destructivas sobre datos de producción sin
autorización explícita.

---

# Healthchecks

Utilizar healthchecks cuando exista una dependencia real del estado saludable
de otro servicio.

`depends_on` no debe utilizarse como sustituto de una estrategia correcta de
disponibilidad.

Cuando el orden de arranque dependa de disponibilidad real, combinar healthchecks
con una condición de servicio saludable o implementar reintentos acotados en el
consumidor.

---

# Frontend estático

No utilizar `ng serve` como servidor de producción. Compilar Angular en una etapa de
build y servir los archivos estáticos con Nginx u otro servidor apropiado.

El servidor deberá incluir fallback a `index.html` para rutas de la SPA. Los assets
con hash pueden usar caché prolongada; `index.html` no deberá quedar sujeto a una
caché que impida recibir una nueva versión.

Si Nginx actúa como reverse proxy, el navegador deberá consumir una ruta pública
válida; nunca nombres DNS internos de Compose como `backend`.

---

# Reproducibilidad y operación

Enviar logs de los contenedores a `stdout` y `stderr` salvo que exista una solución de
observabilidad que requiera otra estrategia.

Omitir pruebas dentro del build de imagen solo es aceptable cuando la verificación se
ejecuta como una etapa independiente y obligatoria del flujo de entrega.

Antes de considerar listo un cambio relevante de despliegue, verificar el arranque
desde un checkout limpio con la configuración de ejemplo, la salud de los servicios,
la persistencia de volúmenes y una ruta profunda del frontend recargada directamente.

---

# Cambios de infraestructura

Antes de realizar un cambio que pueda afectar:

- datos persistentes;
- puertos;
- redes;
- variables de entorno;
- disponibilidad;
- compatibilidad con producción;

el agente deberá explicar el impacto antes de aplicarlo.
