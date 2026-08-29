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

---

# Versiones

Evitar `latest`.

Utilizar versiones explícitas cuando sea razonable.

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