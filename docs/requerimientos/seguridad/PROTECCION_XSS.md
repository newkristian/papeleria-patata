# Protección contra Cross-Site Scripting (XSS)

**Estado:** Mitigaciones verificadas en módulo de productos  
**Última revisión:** 4 de septiembre de 2026

## Objetivo

Reducir el riesgo de que código JavaScript inyectado acceda a credenciales de sesión,
datos sensibles o acciones autenticadas del POS.

## Riesgo conocido

El `accessToken` se conserva en `sessionStorage` y el `refreshToken` en
`localStorage`. Ambos almacenes son accesibles desde JavaScript ejecutado en el mismo
origen. Una vulnerabilidad XSS podría extraerlos; el refresh token tiene mayor impacto
porque su vigencia permite solicitar nuevos access tokens.

## Controles actuales

- Angular escapa interpolaciones de texto de forma predeterminada.
- No se ha identificado uso de renderizado arbitrario mediante `innerHTML` ni
  bypasses de sanitización (`bypassSecurityTrust*`).
- El access token deja de persistir cuando termina la sesión del navegador.
- En el módulo de productos, categorías, proveedores e inventario se aplica data binding
  estricto mediante propiedades nativas y directivas (`[src]`, `[value]`, `{{ }}`).
- Normalización y filtrado de entradas en backend mediante Bean Validation y expresiones
  regulares para códigos de barras y RFC.
- Carga segura de fotografías: `ValidadorSeguridadImagen` descarta archivos ejecutables
  disfrazados, como SVG con `<script>` incrustado o polyglots, requiriendo decodificación
  real en píxeles raster (JPEG/PNG) antes de aceptar el archivo.

## Mitigaciones pendientes

- Evaluar la migración del refresh token a una cookie `HttpOnly`, `Secure` y con una
  política `SameSite` adecuada; requiere cambios coordinados en backend, CORS y CSRF.
- Definir una Content Security Policy restrictiva en Nginx.
- Evitar HTML dinámico no sanitizado y revisar cualquier uso futuro de bypasses de
  sanitización.
- Revisar dependencias frontend y limitar scripts, imágenes y recursos de terceros.
- Rotar o invalidar refresh tokens y limitar su vigencia.

## Criterios de aceptación

- Los tokens de larga duración no son accesibles desde JavaScript del navegador.
- La aplicación no ejecuta contenido proporcionado por usuarios como HTML o scripts.
- La política CSP solo permite los orígenes estrictamente necesarios.
