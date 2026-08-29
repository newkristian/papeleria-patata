> [!CAUTION]
> **DOCUMENTO OBSOLETO.** Este archivo se conserva únicamente como registro
> histórico. No debe utilizarse como fuente de información, planeación ni estado
> actual del proyecto. Su contenido puede contradecir la implementación vigente y
> está sujeto a eliminación posterior. Consulta `docs/requerimientos/`.

# Planeación de Tareas - Fase Alfa

Esta lista es secuencial e incremental. Ejecutaremos una tarea a la vez, haciendo pausas para revisión y commits.

- [x] **Tarea 1: Completar Backend Restante**
  - Implementar CRUD básico en backend de **Clientes**.
  - Implementar CRUD básico en backend de **Proveedores**.
  - Exponer endpoints GET faltantes en `VentaController` (listar ventas, detalle, ventas del día).
  - *Pausa para commit.*

- [x] **Tarea 2: Inicialización del Frontend (Angular 21+)**
  - Ejecutar `ng new frontend` (o `npx @angular/cli new frontend`) en la carpeta raíz.
  - Configurar CSS Vanilla / TailwindCSS.
  - Crear la estructura básica de carpetas (`core`, `shared`, `features`).
  - *Pausa para commit.*

- [x] **Tarea 3: Página de Inicio (Home) Pública**
  - Crear el componente `HomeComponent`.
  - Diseñar el layout público (Header, Hero, Ofertas, Footer).
  - Añadir el botón "Terminal Punto de Venta" en el header superior derecho.
  - *Pausa para commit.*

- [x] **Tarea 4: Módulo de Autenticación y Login**
  - Crear `AuthGuard` e `Interceptor` para JWT.
  - Crear pantalla de Login.
  - Conectar con el endpoint real del backend `/api/v1/auth`.
  - *Pausa para commit.*

- [ ] **Tarea 5: Interfaz del Punto de Venta (POS) - UI Base**
  - Crear el layout protegido (Sidebar/Navbar + Main content).
  - Diseñar la pantalla principal del POS (Buscador a la izquierda, Carrito a la derecha).
  - *Pausa para commit.*

- [ ] **Tarea 6: Integración del POS con Backend**
  - Integrar búsqueda de productos.
  - Integrar la creación de la venta (`POST /ventas`).
  - *Pausa para commit.*

- [ ] **Tarea 7: Preparación VPS y Despliegue**
  - Crear `docker-compose.yml` en la raíz (postgres + backend + frontend nginx).
  - Configurar proxy inverso Nginx.
  - Ajustar `deploy.sh`.
  - *Pausa para commit y despliegue Alfa.*
