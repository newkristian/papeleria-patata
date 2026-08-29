# QA y Verificación

## Objetivo

Verificar que la implementación cumple el requerimiento sin introducir
problemas evidentes.

---

# Verificación por tarea

Después de cada tarea revisar según corresponda:

- compilación;
- errores de tipos;
- imports;
- contratos;
- migraciones;
- seguridad;
- comportamiento esperado.

---

# Backend

Verificar cuando aplique:

- endpoint correcto;
- método HTTP correcto;
- validaciones;
- status codes;
- autorización;
- transacciones;
- manejo de errores;
- persistencia;
- request/response.

---

# Frontend

Verificar cuando aplique:

- compilación TypeScript;
- consumo correcto de API;
- estado;
- loading;
- errores;
- formularios;
- navegación;
- permisos visuales;
- respuesta ante datos vacíos.

---

# Flujo completo

Al finalizar una funcionalidad revisar conceptualmente:

Frontend

↓

API

↓

Aplicación

↓

Dominio

↓

Persistencia

↓

Respuesta

↓

Actualización de interfaz

---

# Seguridad

Toda funcionalidad nueva deberá revisar al menos:

- autenticación requerida;
- autorización correcta;
- validación de inputs;
- exposición de datos;
- acceso a recursos.

---

# Pruebas

No es obligatorio crear pruebas para todo cambio.

Agregar pruebas cuando aporten valor significativo, especialmente en lógica de:

- ventas;
- pagos;
- inventario;
- caja;
- seguridad;
- reglas de negocio críticas.

---

# Finalización

Una funcionalidad no se considera terminada si:

- requiere datos ficticios;
- tiene endpoints pendientes;
- contiene TODOs esenciales;
- depende de backend incompleto;
- tiene errores conocidos que impiden el flujo principal.

Los pendientes menores deberán informarse explícitamente.