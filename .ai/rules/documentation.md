# Documentación

## Idioma

La documentación del proyecto deberá escribirse en español.

Los nombres de clases, métodos, variables, endpoints y conceptos técnicos
seguirán las convenciones habituales de la tecnología y podrán utilizar inglés.

No traducir nombres técnicos cuando hacerlo produzca términos artificiales o
inconsistentes.

---

# Código

El código debe ser suficientemente claro para minimizar comentarios.

Documentar decisiones, restricciones y comportamientos que no sean evidentes.

No documentar literalmente lo que el código ya expresa.

---

# README

Actualizar documentación cuando un cambio modifique:

- instalación;
- configuración;
- despliegue;
- variables de entorno;
- arquitectura relevante;
- proceso necesario para ejecutar el proyecto.

El README raíz deberá permitir comprender y ejecutar el sistema sin depender de una
explicación del autor. Mantenerlo conciso y enlazar documentación especializada
cuando el detalle no pertenezca a la introducción.

Cuando el proyecto se prepare para entrega o portafolio, el README deberá incluir,
según corresponda:

- problema de negocio y alcance real;
- funcionalidades implementadas, sin presentar pendientes como terminados;
- stack y arquitectura en un nivel útil;
- requisitos previos y comandos comprobados de instalación, pruebas y ejecución;
- configuración mediante una plantilla sin secretos;
- seguridad y roles relevantes;
- acceso a OpenAPI cuando esté habilitado;
- decisiones técnicas importantes y roadmap claramente separado.

Las capturas y otros recursos del repositorio deberán utilizar rutas relativas y
nombres descriptivos. No incluir rutas locales del equipo del desarrollador.

Los comandos documentados deberán haberse verificado. Si dependen de un ambiente,
perfil, permiso o servicio externo, esa condición deberá indicarse expresamente.

---

# TODO

Un TODO deberá indicar una acción concreta.

Evitar TODOs vagos como:

```text
TODO mejorar
```

Preferir:

```text
TODO: reemplazar esta consulta cuando se migre el módulo de inventario al nuevo puerto de persistencia.
```

---

# Documentación de API

Los contratos REST públicos deberán mantenerse documentados de forma
consistente con la implementación.

La documentación nunca deberá describir comportamiento que el sistema ya no
posea.
