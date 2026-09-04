# Papelería Patata — Sistema POS (Point of Sale)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22-red.svg)](https://angular.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

Sistema integral de Punto de Venta (POS) diseñado para la administración operativa, gestión de catálogo de productos, control de inventario, promociones, ventas y reportería para negocios minoristas.

---

## 🚀 Arquitectura y Stack Tecnológico

El proyecto está estructurado como una aplicación desacoplada con backend REST y cliente SPA en frontend:

### Backend
- **Lenguaje / Framework:** Java 21 & Spring Boot 4.0.
- **Seguridad:** Spring Security con autenticación y autorización basadas en JWT (Stateless) y hashing BCrypt.
- **Persistencia:** PostgreSQL 16 con Spring Data JPA y migraciones de esquema versionadas mediante **Flyway**.
- **Documentación de API:** OpenAPI 3 / Springdoc.
- **Arquitectura:** Hexagonal Simplificada de adopción progresiva.

### Frontend
- **Framework:** Angular 22 (Componentes Standalone y gestión de estado con Signals).
- **Estilos:** Tailwind CSS.
- **Servidor WEB / SPA:** Nginx en entorno contenedorizado con política de no-cache para `index.html` e inmunidad de assets hash.

### Infraestructura
- **Orquestación:** Docker & Docker Compose (perfiles de desarrollo y producción aislados).

---

## 🛠️ Requisitos Previos

- **Docker y Docker Compose** (versión reciente compatible con Compose V2).
- *Opción de desarrollo manual:* **JDK 21**, **Maven 3.9+** y **Node.js 22+**.

---

## 📦 Guía de Inicio Rápido

### 1. Configuración de Variables de Entorno

Antes de iniciar el sistema, genera tu archivo `.env` local a partir de la plantilla:

```bash
cp .env.example .env
```

Ajusta las variables en `.env` según tu entorno de trabajo (en desarrollo los valores por defecto funcionan directamente).

---

### 2. Despliegue con Docker Compose (Recomendado)

#### Entorno de Desarrollo (DEV)
Levanta la base de datos PostgreSQL, el backend y el frontend Angular con proxy inverso Nginx:

```bash
docker compose up -d
```

- **Frontend (Angular / Nginx):** `http://localhost:4200`
- **Backend API (Spring Boot):** `http://localhost:8080/api/v1`
- **Swagger UI (Documentación de API):** `http://localhost:8080/swagger-ui.html`

#### Entorno de Producción (PROD)
Aplica la configuración productiva desactivando fallbacks y exigiendo secretos reales en el entorno:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

*(Nota: En modo `prod`, Swagger UI y las especificaciones JSON quedan deshabilitadas por política de seguridad).*

---

### 3. Ejecución Manual (Sin Docker)

#### Base de Datos PostgreSQL
Asegúrate de contar con un servidor PostgreSQL ejecutándose y crea la base de datos de desarrollo:
```sql
CREATE DATABASE posdb_dev;
CREATE USER posuser WITH PASSWORD 'pospass';
GRANT ALL PRIVILEGES ON DATABASE posdb_dev TO posuser;
```

#### Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend (Angular)
```bash
cd frontend
npm install
npm start
```

---

## 🧪 Pruebas Automatizadas

### Backend
Para ejecutar las suites de pruebas unitarias e integración:

```bash
cd backend
./mvnw test
```

---

## 📚 Documentación del Proyecto

Toda la documentación técnica y de negocio se encuentra centralizada en las siguientes carpetas:

- [`docs/requerimientos/`](file:///Users/krisconk/FullStack/papeleria-patata/docs/requerimientos/README.md): Especificación de requisitos de negocio por módulo (Inventario, Ventas, Promociones, Clientes, etc.).
- [`docs/trabajo-pendiente/`](file:///Users/krisconk/FullStack/papeleria-patata/docs/trabajo-pendiente/): Trabajo pendiente de buenas prácticas, seguridad e infraestructura.
- [`.ai/README.md`](file:///Users/krisconk/FullStack/papeleria-patata/.ai/README.md): Guías de arquitectura, estándares de codificación y gobernanza para agentes de IA.

---

## 🛡️ Seguridad

- Archivos `.env` ignorados formalmente en Git.
- Fail-fast implementado en perfil productivo para garantizar la presencia de credenciales y secretos JWT.
- Contraseñas almacenadas de forma segura utilizando salteado y hash BCrypt.
- Rutas de administración y venta protegidas mediante RBAC (Role-Based Access Control).
