# Corrección de Errores

## 1. Reproducir o comprender

Antes de modificar código, determinar:

- comportamiento actual;
- comportamiento esperado;
- condiciones bajo las cuales ocurre;
- capa donde probablemente se origina.

---

# 2. Buscar causa raíz

No corregir únicamente el síntoma si existe una causa clara.

Analizar el flujo completo cuando sea necesario.

---

# 3. Evaluar impacto

Determinar si la corrección puede afectar:

- otros casos de uso;
- persistencia;
- API;
- frontend;
- seguridad.

---

# 4. Plan

Presentar:

- causa identificada;
- corrección propuesta;
- archivos afectados;
- tareas necesarias.

Esperar aprobación.

---

# 5. Implementación

Realizar el cambio mínimo que resuelva correctamente la causa.

No aprovechar un bug fix para realizar refactorizaciones no relacionadas.

---

# 6. Regresión

Cuando el error sea crítico o fácilmente reproducible, considerar agregar una
prueba que impida su reaparición.

Especialmente para errores relacionados con:

- ventas;
- dinero;
- inventario;
- autenticación;
- autorización;
- pérdida de datos.