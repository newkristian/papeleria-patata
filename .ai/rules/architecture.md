# Arquitectura

## Arquitectura objetivo

El backend utiliza una Arquitectura Hexagonal Simplificada.

Su objetivo es separar:

- dominio
- casos de uso
- entrada al sistema
- acceso a infraestructura

La arquitectura debe mantenerse pragmática.

No deberán crearse abstracciones que no aporten una separación real de responsabilidades.

---

# Capas

## Domain

Contiene conceptos y reglas propias del negocio.

Puede contener:

- modelos de dominio
- reglas de negocio
- excepciones de dominio
- value objects

El dominio debe evitar dependencias innecesarias de infraestructura.

No debe depender de:

- Controllers
- DTOs HTTP
- Spring Data repositories
- detalles de PostgreSQL
- mecanismos de autenticación HTTP

---

## Application

Contiene los casos de uso del sistema.

Puede contener:

- servicios de aplicación
- puertos de entrada
- puertos de salida
- DTOs internos cuando sean necesarios

Coordina el dominio y los puertos necesarios para completar un caso de uso.

Los límites transaccionales normalmente pertenecen a esta capa.

---

## Adapters

Permiten que sistemas externos interactúen con la aplicación.

### Adaptadores de entrada

Ejemplos:

- REST Controllers

Son responsables de:

- recibir solicitudes
- validar estructura de entrada
- convertir datos
- invocar casos de uso
- generar respuestas

No contienen reglas de negocio.

### Adaptadores de salida

Ejemplos:

- persistencia JPA
- servicios externos
- almacenamiento de archivos

Implementan los puertos requeridos por la aplicación.

---

## Infrastructure

Contiene configuración y detalles técnicos relacionados con frameworks o
infraestructura.

Ejemplos:

- configuración Spring
- Spring Security
- JWT
- configuración JPA
- clientes externos
- configuración de infraestructura

---

# Regla de dependencias

Las dependencias deberán apuntar hacia el núcleo de la aplicación.

Conceptualmente:

REST -> Application -> Domain

Infrastructure -> Application/Domain

El dominio nunca deberá depender de los adaptadores.

---

# Arquitectura pragmática

No es obligatorio crear una interfaz para cada clase.

Crear un puerto cuando exista una frontera real entre la aplicación y una
dependencia externa.

Ejemplos apropiados:

- persistencia
- almacenamiento
- servicios externos
- envío de correos
- proveedores de pagos

Evitar interfaces cuyo único propósito sea envolver una implementación sin
aportar desacoplamiento.

---

# Migración del código existente

El proyecto puede contener código basado en:

Controller -> Service -> Repository

No deberá realizarse una migración masiva.

Cuando se modifique funcionalidad existente:

1. mantener el comportamiento actual;
2. evitar aumentar la deuda arquitectónica;
3. migrar pequeñas partes cuando sea razonable;
4. priorizar el alcance solicitado.

Una funcionalidad nueva deberá aproximarse a la arquitectura objetivo siempre
que hacerlo no introduzca complejidad desproporcionada.

---

# Reglas obligatorias

No:

Controller -> Repository

No:

Controller -> JPA Entity como contrato HTTP

No:

Domain -> Controller

No:

Domain -> Spring Data Repository

Preferir:

Controller -> Application Service/Use Case -> Port -> Adapter