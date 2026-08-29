# Interfaz base del POS

**Estado:** Aprobado  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Ofrecer una pantalla protegida y táctil con navegación operativa, buscador a la
izquierda y carrito a la derecha.

## Alcance aprobado

- Layout protegido con Sidebar o Navbar y área principal.
- Diseño responsive orientado a tablet y operación rápida.

## Implementación verificada

La ruta `/pos` está protegida, pero todavía carga `HomeComponent` como placeholder.

## Criterios de aceptación

- La ruta presenta un layout POS propio y no reutiliza la página pública.
