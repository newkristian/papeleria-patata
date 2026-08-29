# Desarrollo de Nueva Funcionalidad

## 1. Analizar requerimiento

Aplicar `requirements_analysis.md`.

Identificar:

- reglas de negocio;
- datos requeridos;
- cambios de base de datos;
- cambios backend;
- cambios frontend;
- requisitos de seguridad.

---

# 2. Definir flujo funcional

Antes del código deberá comprenderse el flujo completo.

Ejemplo:

Usuario agrega producto

↓

Frontend envía solicitud

↓

Controller valida contrato

↓

Caso de uso procesa operación

↓

Persistencia almacena información

↓

Backend genera respuesta

↓

Frontend actualiza estado

---

# 3. Crear plan

El orden recomendado es:

1. Base de datos, cuando aplique.
2. Dominio.
3. Aplicación.
4. Persistencia.
5. API REST.
6. Seguridad/autorización.
7. Verificación backend.
8. Frontend.
9. Verificación del flujo completo.

---

# 4. Backend

Implementar todo el soporte requerido antes de iniciar frontend.

Aplicar:

`backend_first.md`

---

# 5. Frontend

Una vez aprobado el backend:

1. definir modelos/contratos;
2. implementar servicio HTTP;
3. implementar estado;
4. implementar componentes;
5. implementar pantalla;
6. manejar estados de carga;
7. manejar errores;
8. verificar permisos visuales cuando corresponda.

---

# 6. Revisión

Cada punto del plan se considera una tarea independiente cuando su tamaño
justifique una revisión.

Aplicar:

`review_and_commit.md`

---

# 7. Verificación final

Aplicar:

`qa_and_verification.md`