package com.kristianconk.api_papeleria.integracion;

import com.kristianconk.api_papeleria.auth.dto.AuthResponse;
import com.kristianconk.api_papeleria.auth.dto.LoginRequest;
import com.kristianconk.api_papeleria.categoria.CategoriaRequestDTO;
import com.kristianconk.api_papeleria.categoria.CategoriaResponseDTO;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.ErrorResponse;
import com.kristianconk.api_papeleria.producto.ProductoDetalleDTO;
import com.kristianconk.api_papeleria.producto.ProductoRequestDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorRequestDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorResponseDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioCreateRequestDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioResponseDTO;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatalogosIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    private String adminToken;
    private String vendedorToken;
    private String gerenteToken;
    private String inventaristaToken;

    @BeforeAll
    void autenticarRoles() {
        adminToken = login("admin@pos.com", "admin123");
        vendedorToken = login("caja@pos.com", "caja123");
        gerenteToken = crearUsuarioYAutenticar(
                "Gerente Catálogos", "gerente.catalogos@pos.com", "Gerente123", RolUsuario.GERENTE);
        inventaristaToken = crearUsuarioYAutenticar(
                "Inventarista Catálogos", "inventario.catalogos@pos.com", "Inventario123", RolUsuario.INVENTARISTA);
    }

    @Test
    void migracionConDatosPreviosCreaUnSoloPendienteYProtegeSuIdentidadYPagos() {
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(1L, contar("SELECT COUNT(*) FROM proveedores WHERE es_sistema = TRUE"));
        assertEquals(1L, contar("""
                SELECT COUNT(*) FROM proveedores
                 WHERE es_sistema = TRUE
                   AND nombre = 'PENDIENTE'
                   AND activo = TRUE
                   AND porcentaje_comision = 0.00
                """));
        assertTrue(contar("SELECT COUNT(*) FROM productos WHERE proveedor_id IN (1, 2)") > 0);

        final DataIntegrityViolationException modificacionRechazada = assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("UPDATE proveedores SET activo = FALSE WHERE es_sistema = TRUE"));
        assertTrue(modificacionRechazada.getMostSpecificCause().getMessage().contains("configuración protegida"));
        final DataIntegrityViolationException eliminacionRechazada = assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM proveedores WHERE es_sistema = TRUE"));
        assertTrue(eliminacionRechazada.getMostSpecificCause().getMessage().contains("configuración protegida"));
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
                INSERT INTO pagos_proveedor (
                    proveedor_id, fecha_inicio, fecha_fin, total_ventas,
                    comision_tienda, monto_pagar, fecha_pago, pagado
                )
                SELECT id, CURRENT_DATE, CURRENT_DATE, 0, 0, 0, CURRENT_DATE, FALSE
                  FROM proveedores
                 WHERE es_sistema = TRUE
                """));
    }

    @Test
    void administradorEInventaristaCreanCategoriaYProductoConCantidadDesconocida() {
        crearCategoriaYProducto(adminToken, "Administración");
        crearCategoriaYProducto(inventaristaToken, "Inventario");
    }

    @Test
    void categoriaDuplicadaDevuelveConflictoControlado() {
        final String nombre = "Categoría duplicada " + UUID.randomUUID().toString().substring(0, 8);
        assertEquals(HttpStatus.CREATED,
                post("/api/v1/categorias", new CategoriaRequestDTO(nombre, null), adminToken,
                        CategoriaResponseDTO.class).getStatusCode());

        final ResponseEntity<ErrorResponse> duplicada = post(
                "/api/v1/categorias",
                new CategoriaRequestDTO(nombre.toUpperCase(), null),
                adminToken,
                ErrorResponse.class);

        assertEquals(HttpStatus.CONFLICT, duplicada.getStatusCode());
        assertNotNull(duplicada.getBody());
        assertTrue(duplicada.getBody().mensaje().contains("Ya existe una categoría"));
    }

    @Test
    void permisosDeCatalogosRechazanRolesNoAutorizados() {
        final CategoriaRequestDTO categoria = new CategoriaRequestDTO("Sin permiso", null);
        assertEquals(HttpStatus.FORBIDDEN,
                post("/api/v1/categorias", categoria, vendedorToken, ErrorResponse.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                get("/api/v1/proveedores", vendedorToken, ErrorResponse.class).getStatusCode());

        final ProveedorRequestDTO proveedor = proveedorRequest("Proveedor restringido");
        assertEquals(HttpStatus.FORBIDDEN,
                post("/api/v1/proveedores", proveedor, inventaristaToken, ErrorResponse.class).getStatusCode());

        final ProveedorResponseDTO creado = post(
                "/api/v1/proveedores", proveedor, adminToken, ProveedorResponseDTO.class).getBody();
        assertNotNull(creado);
        assertEquals(HttpStatus.FORBIDDEN,
                delete("/api/v1/proveedores/" + creado.id(), gerenteToken, ErrorResponse.class).getStatusCode());
    }

    @Test
    void proveedorPendienteNoApareceEnSelectorYDatosInvalidosDevuelven400() {
        final ResponseEntity<ProveedorResponseDTO[]> listado =
                get("/api/v1/proveedores", inventaristaToken, ProveedorResponseDTO[].class);
        assertEquals(HttpStatus.OK, listado.getStatusCode());
        assertNotNull(listado.getBody());
        assertTrue(listado.getBody().length >= 2);
        for (final ProveedorResponseDTO proveedor : listado.getBody()) {
            assertFalse(proveedor.sistema());
            assertTrue(proveedor.activo());
        }

        final ProveedorRequestDTO invalido = new ProveedorRequestDTO(
                "Proveedor inválido", "RFC-INVALIDO", "abc", "correo-invalido", null,
                new BigDecimal("101.00"));
        assertEquals(HttpStatus.BAD_REQUEST,
                post("/api/v1/proveedores", invalido, adminToken, ErrorResponse.class).getStatusCode());
    }

    @Test
    void desactivarProveedorReasignaProductosYLaOperacionEsAtomica() {
        final ProveedorResponseDTO proveedor = post(
                "/api/v1/proveedores", proveedorRequest("Proveedor a desactivar"), adminToken,
                ProveedorResponseDTO.class).getBody();
        assertNotNull(proveedor);
        final Long productoId = crearProducto(adminToken, 1L, proveedor.id(), "Desactivación").id();

        assertEquals(HttpStatus.NO_CONTENT,
                delete("/api/v1/proveedores/" + proveedor.id(), adminToken, Void.class).getStatusCode());
        assertEquals(0L, contar("SELECT COUNT(*) FROM productos WHERE id = " + productoId
                + " AND proveedor_id = " + proveedor.id()));
        assertEquals(1L, contar("""
                SELECT COUNT(*)
                  FROM productos p
                  JOIN proveedores pr ON pr.id = p.proveedor_id
                 WHERE p.id = %d AND pr.es_sistema = TRUE
                """.formatted(productoId)));
        assertEquals(1L, contar("SELECT COUNT(*) FROM proveedores WHERE id = " + proveedor.id()
                + " AND activo = FALSE"));

        final ProveedorResponseDTO proveedorConFallo = post(
                "/api/v1/proveedores", proveedorRequest("Proveedor rollback"), adminToken,
                ProveedorResponseDTO.class).getBody();
        assertNotNull(proveedorConFallo);
        final Long productoRollbackId = crearProducto(adminToken, 1L, proveedorConFallo.id(), "Rollback").id();
        instalarBloqueoDePrueba(proveedorConFallo.id());
        try {
            assertEquals(HttpStatus.CONFLICT,
                    delete("/api/v1/proveedores/" + proveedorConFallo.id(), adminToken, ErrorResponse.class)
                            .getStatusCode());
        } finally {
            retirarBloqueoDePrueba();
        }
        assertEquals(1L, contar("SELECT COUNT(*) FROM productos WHERE id = " + productoRollbackId
                + " AND proveedor_id = " + proveedorConFallo.id()));
        assertEquals(1L, contar("SELECT COUNT(*) FROM proveedores WHERE id = " + proveedorConFallo.id()
                + " AND activo = TRUE"));
    }

    private void crearCategoriaYProducto(final String token, final String sufijo) {
        final String nombre = "Categoría IT " + sufijo + " " + UUID.randomUUID().toString().substring(0, 8);
        final ResponseEntity<CategoriaResponseDTO> categoria = post(
                "/api/v1/categorias", new CategoriaRequestDTO(nombre, "Creada por prueba de integración"),
                token, CategoriaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, categoria.getStatusCode());
        assertNotNull(categoria.getBody());

        final ProductoDetalleDTO producto = crearProducto(token, categoria.getBody().id(), 1L, sufijo);
        assertEquals(categoria.getBody().id(), producto.categoria().id());
        assertTrue(producto.cantidadDesconocida());
        assertEquals(0, producto.stockActual());
    }

    private ProductoDetalleDTO crearProducto(
            final String token,
            final Long categoriaId,
            final Long proveedorId,
            final String sufijo) {
        final String codigo = "IT" + UUID.randomUUID().toString().replace("-", "");
        final ProductoRequestDTO request = new ProductoRequestDTO(
                codigo,
                "Producto IT " + sufijo,
                "Producto temporal para validar catálogos",
                categoriaId,
                proveedorId,
                new BigDecimal("10.00"),
                5,
                "pieza",
                null,
                true,
                true);
        final ResponseEntity<ProductoDetalleDTO> respuesta =
                post("/api/v1/productos", request, token, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        return respuesta.getBody();
    }

    private ProveedorRequestDTO proveedorRequest(final String nombreBase) {
        return new ProveedorRequestDTO(
                nombreBase + " " + UUID.randomUUID().toString().substring(0, 8),
                null, "555-123-4567", "catalogos@proveedor.test", "Contacto IT", new BigDecimal("10.00"));
    }

    private String crearUsuarioYAutenticar(
            final String nombre,
            final String email,
            final String password,
            final RolUsuario rol) {
        final UsuarioCreateRequestDTO request = new UsuarioCreateRequestDTO(
                nombre, "Pruebas", email, password, rol, 1L);
        final ResponseEntity<UsuarioResponseDTO> creado =
                post("/api/v1/usuarios", request, adminToken, UsuarioResponseDTO.class);
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        return login(email, password);
    }

    private String login(final String username, final String password) {
        final ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", new LoginRequest(username, password), AuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody().accessToken();
    }

    private <T> ResponseEntity<T> post(
            final String path, final Object body, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers(token)), tipo);
    }

    private <T> ResponseEntity<T> get(final String path, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers(token)), tipo);
    }

    private <T> ResponseEntity<T> delete(final String path, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers(token)), tipo);
    }

    private HttpHeaders headers(final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private long contar(final String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private void instalarBloqueoDePrueba(final Long proveedorId) {
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION bloquear_desactivacion_catalogos_it()
                RETURNS TRIGGER AS $$
                BEGIN
                    IF OLD.id = %d AND NEW.activo = FALSE THEN
                        RAISE EXCEPTION 'Fallo controlado para probar rollback' USING ERRCODE = '23514';
                    END IF;
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql
                """.formatted(proveedorId));
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_bloquear_desactivacion_catalogos_it
                BEFORE UPDATE ON proveedores
                FOR EACH ROW EXECUTE FUNCTION bloquear_desactivacion_catalogos_it()
                """);
    }

    private void retirarBloqueoDePrueba() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bloquear_desactivacion_catalogos_it ON proveedores");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS bloquear_desactivacion_catalogos_it()");
    }
}
