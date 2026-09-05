# Guía de Errores Comunes y Aprendizajes: PostgreSQL + Hibernate en Spring Boot

Este documento recopila problemas reales detectados en el proyecto, sus causas raíz y las soluciones de diseño aprobadas al trabajar con **Spring Data JPA**, **Hibernate 6/7** y **PostgreSQL 16+**.

Los agentes de IA deben consultar obligatoriamente esta guía antes de escribir consultas JPQL con filtros opcionales o depurar errores 500 originados en la capa de persistencia.

---

## 1. El Problema de Inferencia de Tipos con Parámetros Nulos

### Síntomas
Al invocar endpoints que realizan búsquedas paginadas con filtros opcionales (por ejemplo `GET /api/v1/inventario/movimientos?page=0&size=20` sin filtros):

1. **Error de tipo no determinable:**
   ```text
   org.hibernate.exception.SQLGrammarException: JDBC exception executing SQL
   Caused by: org.postgresql.util.PSQLException: ERROR: could not determine data type of parameter $7
   ```
2. **Error de función inexistente con bytea:**
   ```text
   org.postgresql.util.PSQLException: ERROR: function lower(bytea) does not exist
   ```

### Causa Raíz
PostgreSQL tiene un tipado estricto en su analizador de consultas (`planner/analyzer`). 

Cuando se escribe una consulta JPQL con el antipatrón de "filtro opcional estático":
```sql
-- ANTIPATRÓN: No usar en PostgreSQL
SELECT m FROM Movimiento m WHERE
(:param IS NULL OR m.campo = :param) AND
(:fecha IS NULL OR m.fecha >= :fecha) AND
(:tipo IS NULL OR m.tipo = :tipo)
```

Hibernate traduce esto a sentencias con marcadores JDBC posicionales `?`:
```sql
WHERE (? IS NULL OR m.campo = ?) AND (? IS NULL OR m.fecha >= ?) AND (? IS NULL OR m.tipo = ?)
```

Si el cliente no envía esos filtros en la petición HTTP, Spring pasa `null` a Hibernate y el driver JDBC de PostgreSQL envía el parámetro con tipo `Types.NULL` (o `OTHER`). En cláusulas como `(? IS NULL)`, PostgreSQL no tiene contexto de columna para inferir el tipo de dato del parámetro `$N`, especialmente para:
- **`LocalDateTime` / `Instant` / Fechas:** Fallo inmediato `could not determine data type of parameter`.
- **`Enum`:** Fallo de inferencia o incompatibilidad de casteo.
- **Funciones de cadenas con `null`:** `LOWER(CONCAT('%', :termino, '%'))` con `:termino = null` evalúa a un tipo `bytea/null` provocando `function lower(bytea) does not exist`.

Además de provocar errores 500, las cláusulas `(? IS NULL OR ...)` destruyen el rendimiento en PostgreSQL porque el optimizador no puede elegir índices en tiempo de compilación de la consulta, forzando escaneos secuenciales de tablas (`Seq Scan`).

---

## 2. Solución Aprobada: Especificaciones Dinámicas (`JpaSpecificationExecutor`)

Para cualquier búsqueda o listado que admita filtros opcionales y paginación, **NO** se deben usar consultas `@Query` JPQL monolíticas con `(:param IS NULL OR ...)`.

La solución estándar, limpia y aprobada en este proyecto es utilizar `Specification` de Spring Data JPA mediante `JpaSpecificationExecutor`.

### Estructura de Implementación

#### 1. En el Repositorio
Extender de `JpaSpecificationExecutor<Entidad>` y definir el método con `Specification`:

```java
package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface InventarioMovimientoRepository 
        extends JpaRepository<InventarioMovimiento, Long>, JpaSpecificationExecutor<InventarioMovimiento> {

    default Page<InventarioMovimiento> buscarMovimientos(
            Long productoId,
            TipoMovimiento tipo,
            Long usuarioId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        
        final Specification<InventarioMovimiento> spec = (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            // Se agregan predicados ÚNICAMENTE si el parámetro no es nulo
            if (productoId != null) {
                predicates.add(cb.equal(root.get("producto").get("id"), productoId));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaMovimiento"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaMovimiento"), fechaFin));
            }

            // Si no hay filtros, devuelve null (sin cláusula WHERE)
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
```

### Por qué esta solución es superior
1. **Cero cláusulas fantasma:** Cuando todos los filtros son `null` (como en la carga inicial de una pantalla), Spring Data ejecuta `SELECT * FROM tabla LIMIT ? OFFSET ?` sin `WHERE` ni parámetros nulos.
2. **Tipado estricto garantizado:** Cuando un filtro sí está presente, Hibernate asocia el valor directamente con el atributo de la entidad en el metamodelo (`root.get("fechaMovimiento")`), indicando a JDBC el tipo exacto (`Types.TIMESTAMP`, `Types.VARCHAR`, `Types.BIGINT`, etc.).
3. **Optimización de índices:** PostgreSQL utiliza índices B-Tree específicos para los filtros activos sin degradación por condiciones disyuntivas `OR`.

---

## 3. Búsqueda de Texto con Like y Parámetros Nulos

Cuando se use texto en búsquedas LIKE:
1. **Normalizar el patrón en Java antes del repositorio:**
   ```java
   final String patron = (termino == null || termino.isBlank())
           ? null
           : "%" + termino.trim().toLowerCase() + "%";
   ```
2. **Evitar funciones SQL sobre parámetros:**
   - ❌ Incorrecto: `LOWER(CONCAT('%', :termino, '%'))`
   - ✅ Correcto: `LOWER(p.nombre) LIKE :patron` (donde `:patron` ya viene en minúsculas y con comodines desde el servicio Java).

---

## 4. Regla para Agentes de IA

1. Al implementar endpoints de búsqueda con paginación (`Pageable`) y filtros opcionales:
   - **Usar siempre `JpaSpecificationExecutor` y `Specification`**.
   - **Nunca** generar consultas JPQL con `(:param IS NULL OR campo = :param)` para fechas, horas, timestamps, enums o tipos complejos.
2. En pruebas de integración (`*IntegrationTest.java`):
   - Probar obligatoriamente el escenario donde todos los parámetros son nulos (`/endpoint?page=0&size=20`) sobre Testcontainers con PostgreSQL real para garantizar que Hibernate y PostgreSQL no fallen en producción.
