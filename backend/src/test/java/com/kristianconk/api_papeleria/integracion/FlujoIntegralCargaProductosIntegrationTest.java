package com.kristianconk.api_papeleria.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kristianconk.api_papeleria.auth.dto.AuthResponse;
import com.kristianconk.api_papeleria.auth.dto.LoginRequest;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.ErrorResponse;
import com.kristianconk.api_papeleria.inventario.AjusteInventarioDTO;
import com.kristianconk.api_papeleria.producto.ProductoActualizarRequestDTO;
import com.kristianconk.api_papeleria.producto.ProductoCrearRequestDTO;
import com.kristianconk.api_papeleria.producto.ProductoDetalleDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorRequestDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorResponseDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioCreateRequestDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioResponseDTO;
import com.kristianconk.api_papeleria.ventas.DetalleVentaRequestDTO;
import com.kristianconk.api_papeleria.ventas.VentaRequestDTO;
import com.kristianconk.api_papeleria.ventas.VentaResponseDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tarea 12: QA Integral, seguridad, concurrencia y verificación de flujos completos.
 * Valida los flujos de negocio punta a punta sobre un contenedor PostgreSQL 16 limpio.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlujoIntegralCargaProductosIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private String adminToken;
    private String gerenteToken;
    private String inventaristaToken;
    private String vendedorToken;

    @BeforeAll
    void setupUsuarios() {
        adminToken = login("admin@pos.com", "admin123");
        vendedorToken = login("caja@pos.com", "caja123");

        gerenteToken = crearUsuarioYAutenticar(
                "Gerente QA", "gerente.qa@pos.com", "Gerente123", RolUsuario.GERENTE);
        inventaristaToken = crearUsuarioYAutenticar(
                "Inventarista QA", "inventario.qa@pos.com", "Inventario123", RolUsuario.INVENTARISTA);
    }

    /**
     * Flujo Real Completo:
     * 1. Crear proveedor.
     * 2. Crear producto con cantidad desconocida (por contar).
     * 3. Subir foto (pipeline asíncrono multipart).
     * 4. Vender con cantidad desconocida (debe procesar sin bloquear stock).
     * 5. Conteo físico inicial mediante ajuste absoluto (fija stock real y apaga cantidadDesconocida).
     * 6. Venta con control de stock (descuenta stock).
     * 7. Venta con stock insuficiente (rechazada).
     * 8. Edición de producto.
     * 9. Desactivación lógica de producto (deja de poder venderse).
     */
    @Test
    void flujoCompleto_Proveedor_Producto_Foto_VentaDesconocida_Conteo_VentaControlada_Desactivacion() {
        final String sufijo = UUID.randomUUID().toString().substring(0, 8);

        // 1. Crear proveedor
        final ProveedorRequestDTO nuevoProv = new ProveedorRequestDTO(
                "Proveedor Integral " + sufijo,
                "PRO991231A12",
                "5551234567",
                "contacto@integral.com",
                "Ing. Pérez",
                new BigDecimal("15.00"));

        final ResponseEntity<ProveedorResponseDTO> provResp = post(
                "/api/v1/proveedores", nuevoProv, adminToken, ProveedorResponseDTO.class);
        assertEquals(HttpStatus.CREATED, provResp.getStatusCode());
        assertNotNull(provResp.getBody());
        final Long provId = provResp.getBody().id();

        // 2. Crear producto con cantidad desconocida
        final String codigoBarras = "7509" + sufijo;
        final ProductoCrearRequestDTO nuevoProd = new ProductoCrearRequestDTO(
                codigoBarras,
                "Producto QA " + sufijo,
                "Descripción detallada del producto",
                1L, // Categoría Papelería Básica existente en V2
                provId,
                new BigDecimal("20.00"),
                null, // margen sugerido por escala
                "PIEZA",
                new BigDecimal("5.00"),
                true); // cantidad desconocida = true

        final ResponseEntity<ProductoDetalleDTO> prodResp = post(
                "/api/v1/productos", nuevoProd, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.CREATED, prodResp.getStatusCode());
        assertNotNull(prodResp.getBody());
        final Long prodId = prodResp.getBody().id();
        assertTrue(prodResp.getBody().cantidadDesconocida());
        assertEquals(0, prodResp.getBody().stockActual());

        // 3. Subir fotografía mediante multipart/form-data
        // PNG de 1x1 píxel válido para pasar la validación de formato
        final byte[] dummyPng = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89,
                0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
                0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
                0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };

        final MultiValueMap<String, Object> formFoto = new LinkedMultiValueMap<>();
        formFoto.add("archivo", new ByteArrayResource(dummyPng) {
            @Override
            public String getFilename() {
                return "test-product.png";
            }
        });
        formFoto.add("esPrincipal", "true");

        final HttpHeaders fotoHeaders = new HttpHeaders();
        fotoHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        fotoHeaders.setBearerAuth(adminToken);

        final ResponseEntity<String> subidaResp = restTemplate.exchange(
                "/api/v1/productos/" + prodId + "/fotos",
                HttpMethod.POST,
                new HttpEntity<>(formFoto, fotoHeaders),
                String.class);

        assertEquals(HttpStatus.ACCEPTED, subidaResp.getStatusCode());

        // Consultar lista de fotos del producto
        final ResponseEntity<String> listaFotosResp = get(
                "/api/v1/productos/" + prodId + "/fotos", adminToken, String.class);
        assertEquals(HttpStatus.OK, listaFotosResp.getStatusCode());

        // 4. Vender con cantidad desconocida (debe procesar sin bloquear por stock)
        final VentaRequestDTO venta1 = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 3)));
        final ResponseEntity<VentaResponseDTO> venta1Resp = post(
                "/api/v1/ventas", venta1, vendedorToken, VentaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, venta1Resp.getStatusCode());

        // Verificar que el producto sigue con cantidad desconocida y stock 0
        final ResponseEntity<ProductoDetalleDTO> prodTrasVenta1 = get(
                "/api/v1/productos/" + prodId, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, prodTrasVenta1.getStatusCode());
        assertTrue(prodTrasVenta1.getBody().cantidadDesconocida());
        assertEquals(0, prodTrasVenta1.getBody().stockActual());

        // 5. Conteo inicial mediante ajuste absoluto (regularización)
        final AjusteInventarioDTO ajuste = new AjusteInventarioDTO(
                prodId, 25, "Conteo físico inicial de regularización", null, true);
        final ResponseEntity<ProductoDetalleDTO> ajusteResp = post(
                "/api/v1/productos/ajustar-inventario", ajuste, inventaristaToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, ajusteResp.getStatusCode());
        assertFalse(ajusteResp.getBody().cantidadDesconocida());
        assertEquals(25, ajusteResp.getBody().stockActual());

        // 6. Venta con control normal de stock (debe descontar de 25)
        final VentaRequestDTO venta2 = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 5)));
        final ResponseEntity<VentaResponseDTO> venta2Resp = post(
                "/api/v1/ventas", venta2, vendedorToken, VentaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, venta2Resp.getStatusCode());

        final ResponseEntity<ProductoDetalleDTO> prodTrasVenta2 = get(
                "/api/v1/productos/" + prodId, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, prodTrasVenta2.getStatusCode());
        assertEquals(20, prodTrasVenta2.getBody().stockActual());
        // 7. Venta con stock insuficiente (pide 30 cuando solo hay 20 disponibles)
        final VentaRequestDTO ventaInsuficiente = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 30)));
        final ResponseEntity<String> ventaInsuficienteResp = post(
                "/api/v1/ventas", ventaInsuficiente, vendedorToken, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, ventaInsuficienteResp.getStatusCode());
        assertTrue(ventaInsuficienteResp.getBody().contains("Stock insuficiente"));

        // 8. Edición de producto
        final ProductoActualizarRequestDTO updateDto = new ProductoActualizarRequestDTO(
                codigoBarras,
                "Producto QA " + sufijo + " - Editado",
                "Descripción modificada",
                1L,
                provId,
                new BigDecimal("22.50"),
                10,
                "PIEZA",
                new BigDecimal("40.00"));

        final ResponseEntity<ProductoDetalleDTO> updateResp = put(
                "/api/v1/productos/" + prodId, updateDto, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, updateResp.getStatusCode());
        assertEquals("Producto QA " + sufijo + " - Editado", updateResp.getBody().nombre());

        // 9. Desactivación lógica de producto
        final ResponseEntity<ProductoDetalleDTO> desactivarResp = patch(
                "/api/v1/productos/" + prodId + "/desactivar", adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, desactivarResp.getStatusCode());
        assertFalse(desactivarResp.getBody().activo());

        // 10. Intentar vender producto desactivado (debe fallar)
        final VentaRequestDTO ventaInactivo = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 1)));
        final ResponseEntity<String> ventaInactivoResp = post(
                "/api/v1/ventas", ventaInactivo, vendedorToken, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, ventaInactivoResp.getStatusCode());
        assertTrue(ventaInactivoResp.getBody().contains("no está activo") ||
                ventaInactivoResp.getBody().contains("inactivo"));
    }

    /**
     * Flujo Alterno:
     * 1. Crear producto sin proveedor -> Se asigna automáticamente a PENDIENTE (sistema = true).
     * 2. Asignar un proveedor real.
     * 3. Desactivar el proveedor real -> El producto se reasigna automáticamente en bloque a PENDIENTE.
     */
    @Test
    void flujoAlterno_ProveedorPendiente_ReasignacionAutomaticaAlDesactivar() {
        final String sufijo = UUID.randomUUID().toString().substring(0, 8);
        final String codigo = "7508" + sufijo;

        // 1. Crear producto sin proveedor (proveedorId = null)
        final ProductoCrearRequestDTO nuevoProd = new ProductoCrearRequestDTO(
                codigo, "Producto Huérfano " + sufijo, null, 1L, null,
                new BigDecimal("10.00"), null, "PIEZA", new BigDecimal("5.00"), true);

        final ResponseEntity<ProductoDetalleDTO> prodResp = post(
                "/api/v1/productos", nuevoProd, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.CREATED, prodResp.getStatusCode());
        assertNotNull(prodResp.getBody());
        assertEquals("PENDIENTE", prodResp.getBody().proveedor().nombre());
        final Long prodId = prodResp.getBody().id();

        // 2. Crear proveedor real y asociarlo al producto
        final ProveedorRequestDTO provDto = new ProveedorRequestDTO(
                "Proveedor Temporal " + sufijo, "PRV991231B12", null, null, null, new BigDecimal("10.00"));
        final ResponseEntity<ProveedorResponseDTO> provResp = post(
                "/api/v1/proveedores", provDto, adminToken, ProveedorResponseDTO.class);
        final Long provRealId = provResp.getBody().id();

        final ProductoActualizarRequestDTO updateDto = new ProductoActualizarRequestDTO(
                codigo, "Producto Reasignado", null, 1L, provRealId,
                new BigDecimal("12.00"), 5, "PIEZA", null);
        final ResponseEntity<ProductoDetalleDTO> prodActualizado = put(
                "/api/v1/productos/" + prodId, updateDto, adminToken, ProductoDetalleDTO.class);
        assertEquals(provRealId, prodActualizado.getBody().proveedor().id());

        // 3. Desactivar el proveedor real
        final ResponseEntity<String> desactivarProvResp = delete(
                "/api/v1/proveedores/" + provRealId, adminToken, String.class);
        assertEquals(HttpStatus.NO_CONTENT, desactivarProvResp.getStatusCode());

        // Verificar que el producto regresó a PENDIENTE automáticamente
        final ResponseEntity<ProductoDetalleDTO> prodVerificado = get(
                "/api/v1/productos/" + prodId, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, prodVerificado.getStatusCode());
        assertEquals("PENDIENTE", prodVerificado.getBody().proveedor().nombre());
    }

    /**
     * Matriz de Roles y Límites de Seguridad:
     * - VENDEDOR no puede dar de alta productos, ni proveedores, ni ver costos unitarios.
     * - INVENTARISTA no puede gestionar proveedores (solo ADMIN y GERENTE).
     * - Ocultamiento de costos de compra para VENDEDOR en historial de movimientos.
     */
    @Test
    void matrizRoles_VerificarRestriccionesPorEndpoint() {
        final String sufijo = UUID.randomUUID().toString().substring(0, 8);

        // VENDEDOR intentando dar de alta producto
        final ProductoCrearRequestDTO prodDtoVendedor = new ProductoCrearRequestDTO(
                "7507" + sufijo, "Prod Ilegal", null, 1L, null,
                new BigDecimal("10.00"), null, "PIEZA", null, true);
        assertEquals(HttpStatus.FORBIDDEN, post("/api/v1/productos", prodDtoVendedor, vendedorToken, String.class).getStatusCode());

        // VENDEDOR intentando dar de alta proveedor
        final ProveedorRequestDTO provDto = new ProveedorRequestDTO(
                "Prov Ilegal " + sufijo, null, null, null, null, new BigDecimal("10.00"));
        assertEquals(HttpStatus.FORBIDDEN, post("/api/v1/proveedores", provDto, vendedorToken, String.class).getStatusCode());

        // INVENTARISTA intentando dar de alta proveedor
        assertEquals(HttpStatus.FORBIDDEN, post("/api/v1/proveedores", provDto, inventaristaToken, String.class).getStatusCode());

        // INVENTARISTA sí puede dar de alta producto (con porcentaje manual nulo para cálculo automático)
        final ProductoCrearRequestDTO prodDtoInventarista = new ProductoCrearRequestDTO(
                "7506" + sufijo, "Prod Inventarista " + sufijo, null, 1L, null,
                new BigDecimal("15.00"), null, "PIEZA", null, true);
        final ResponseEntity<ProductoDetalleDTO> invProdResp = post(
                "/api/v1/productos", prodDtoInventarista, inventaristaToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.CREATED, invProdResp.getStatusCode());

        // Consulta de movimientos: VENDEDOR debe tener campos costoUnitario en null
        final ResponseEntity<String> movsVendedor = get(
                "/api/v1/inventario/movimientos?page=0&size=20", vendedorToken, String.class);
        assertEquals(HttpStatus.OK, movsVendedor.getStatusCode());
        try {
            final JsonNode rootNode = objectMapper.readTree(movsVendedor.getBody());
            final JsonNode content = rootNode.get("content");
            if (content != null && content.isArray()) {
                for (JsonNode mov : content) {
                    assertTrue(mov.get("costoUnitario").isNull(), "El costo unitario debe ser null para rol VENDEDOR");
                }
            }
        } catch (Exception e) {
            fail("Error al procesar JSON de movimientos: " + e.getMessage());
        }
    }

    // Métodos utilitarios HTTP
    private String login(final String username, final String password) {
        final LoginRequest creds = new LoginRequest(username, password);
        final ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/v1/auth/login", creds, AuthResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody().accessToken();
    }

    private String crearUsuarioYAutenticar(
            final String nombre, final String email, final String password, final RolUsuario rol) {
        final UsuarioCreateRequestDTO nuevo = new UsuarioCreateRequestDTO(
                nombre, "Test", email, password, rol, 1L);
        final ResponseEntity<UsuarioResponseDTO> resp = post(
                "/api/v1/usuarios", nuevo, adminToken, UsuarioResponseDTO.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        return login(email, password);
    }

    private <T> ResponseEntity<T> post(
            final String path, final Object body, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers(token)), tipo);
    }

    private <T> ResponseEntity<T> put(
            final String path, final Object body, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers(token)), tipo);
    }

    private <T> ResponseEntity<T> patch(
            final String path, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.PATCH, new HttpEntity<>(headers(token)), tipo);
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
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}
