# Backend

## Stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Lombok

Utilizar características compatibles con las versiones configuradas realmente
en el proyecto.

---

# Inyección de dependencias

Utilizar inyección por constructor.

Preferir Lombok:

```java
@RequiredArgsConstructor
@Service
public class SaleService {

    private final SaleRepository saleRepository;
}
```

No utilizar `@Autowired` en campos en código de producción.

---

# Controllers

Los Controllers deben ser delgados.

Sus responsabilidades son:

- recibir solicitudes HTTP;
- validar entradas;
- invocar casos de uso;
- devolver respuestas HTTP.

No deberán contener reglas de negocio.

No deberán acceder directamente a repositorios.

---

# Servicios de aplicación

Representan operaciones o casos de uso del sistema.

Ejemplos:

- registrar venta;
- cancelar venta;
- abrir caja;
- cerrar caja;
- registrar producto.

Evitar servicios genéricos excesivamente grandes.

Si una clase acumula responsabilidades de múltiples áreas del negocio deberá
evaluarse su separación.

---

# Anotaciones Spring

Usar las anotaciones por su significado:

- `@RestController`: adaptadores REST.
- `@Service`: servicios de aplicación o servicios Spring.
- `@Repository`: implementaciones de persistencia cuando corresponda.
- `@Configuration`: configuración.
- `@Component`: componentes que no encajen semánticamente en una categoría más específica.

No utilizar `@Component` indiscriminadamente.

---

# Configuración

Preferir `@ConfigurationProperties` para grupos relacionados de configuración.

Evitar múltiples `@Value` cuando representan una misma configuración.

Los secretos nunca deberán escribirse directamente en código.

---

# Transacciones

Los límites transaccionales deben establecerse alrededor de casos de uso que
requieran atomicidad.

Preferir `@Transactional` en métodos concretos.

No utilizar `@Transactional` a nivel de clase únicamente para evitar escribir
la anotación en métodos individuales.

Las operaciones de solo lectura podrán utilizar:

```java
@Transactional(readOnly = true)
```

cuando exista un beneficio o necesidad clara.

No iniciar transacciones desde Controllers.

---

# Lombok

Permitido:

- `@Getter`
- `@Setter` cuando sea realmente necesario
- `@Builder`
- `@RequiredArgsConstructor`
- `@Slf4j`

Evitar `@Data`.

No generar setters indiscriminadamente en objetos que deberían ser inmutables.

Para builders del proyecto se podrá utilizar:

```java
@Builder(setterPrefix = "with")
```

siempre que esta sea la convención existente.

# Records

Preferir records de Java para objetos de valor y DTOs cuando su intención sea:
- representar datos inmutables
- simplificar la sintaxis


---

# Null

Evitar utilizar `null` como resultado normal de operaciones.

Para búsquedas que pueden no encontrar un resultado, utilizar `Optional`
cuando represente correctamente el contrato.

No utilizar `Optional`:

- como atributo de entidades;
- como parámetro de métodos;
- indiscriminadamente.

Las colecciones deberían devolver colecciones vacías en lugar de `null`.