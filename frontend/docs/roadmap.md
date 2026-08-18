# Roadmap Frontend - Papelería Patata (MVP Alfa)

**Stack Tecnológico:**
- Framework: Angular v21+ (Single Page Application - SPA)
- Estilos: CSS Vanilla o TailwindCSS (A definir, priorizando diseño moderno y "premium" UI/UX)

## Estructura Principal

### 1. Página de Inicio (Home Pública)
- **Acceso:** Público (Sin login)
- **Objetivo:** Presencia web y escaparate digital.
- **Contenido:**
  - Información básica del negocio (Logo, Ubicación, Horarios).
  - Carrusel/Sección de Ofertas destacadas.
  - Sección de Nuevos Productos.
- **Navegación:**
  - Botón en la esquina superior derecha: **"Terminal Punto de Venta"** -> Redirige a la pantalla de Login / POS.

### 2. Login (Autenticación)
- **Acceso:** Público (Redirigido desde áreas protegidas o Home)
- **Objetivo:** Autenticar empleados (Admin, Gerente, Vendedor) vía JWT.
- **Funcionalidad:**
  - Formulario de Login (Usuario/Password).
  - Integración con `/api/v1/auth/login`.
  - Almacenamiento seguro del JWT (localStorage).

### 3. Terminal Punto de Venta (POS) - MVP Core
- **Acceso:** Restringido (Requiere Login válido)
- **Objetivo:** Interfaz rápida para registrar ventas en la vida real.
- **Componentes:**
  - **Buscador de Productos:** Input rápido optimizado para lector de código de barras o búsqueda por nombre.
  - **Catálogo Rápido:** Grid visual con los productos más comunes.
  - **Carrito / Ticket de Venta (Panel Lateral):**
    - Lista de productos agregados con cantidades modificables.
    - Subtotal y Total dinámico.
    - Selección de Cliente (Default: "Cliente Mostrador").
  - **Checkout (Cobro):**
    - Botón "Cobrar" que envía el payload a `POST /api/v1/ventas`.
    - Modal de confirmación con método de pago (Efectivo/Tarjeta) y cálculo de cambio.

### 4. Módulo de Productos (CRUD Básico)
- **Acceso:** Restringido (Roles Gerente/Admin)
- **Objetivo:** Gestionar el catálogo desde el frontend.
- **Funcionalidad:**
  - Listado en tabla con paginación/búsqueda.
  - Formulario rápido para crear nuevos productos (soporte para flag `cantidadDesconocida`).

## Consideraciones No Funcionales
- **Responsive Design:** Priorizar uso en Tablets o pantallas táctiles (escenario típico de un POS).
- **Manejo de Errores:** Alertas claras si falla la conexión al backend o si hay error de validación.
- **Interceptores JWT:** Angular HTTP Interceptor para adjuntar el token automáticamente a todas las peticiones a `/api/v1/*`.
