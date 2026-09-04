package com.kristianconk.api_papeleria.integracion;

import com.kristianconk.api_papeleria.auth.dto.AuthResponse;
import com.kristianconk.api_papeleria.auth.dto.LoginRequest;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.ErrorResponse;
import com.kristianconk.api_papeleria.inventario.AjusteInventarioDTO;
import com.kristianconk.api_papeleria.inventario.InventarioMovimientoRequestDTO;
import com.kristianconk.api_papeleria.producto.ProductoCrearRequestDTO;
import com.kristianconk.api_papeleria.producto.ProductoDetalleDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioCreateRequestDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioPerfilDTO;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SemanticaInventarioIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;
    private String inventaristaToken;

    @BeforeAll
    void setUpAll() {
        adminToken = login("admin@pos.com", "admin123");

        final UsuarioCreateRequestDTO nuevoInventarista = new UsuarioCreateRequestDTO(
                "Luis", "Inventarios", "inventario.it@pos.com", "Inventario123", RolUsuario.INVENTARISTA, 1L);
        final ResponseEntity<UsuarioResponseDTO> creado = post("/api/v1/usuarios", nuevoInventarista, adminToken, UsuarioResponseDTO.class);
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());

        inventaristaToken = login("inventario.it@pos.com", "Inventario123");
    }

    private String login(final String username, final String password) {
        final ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                new LoginRequest(username, password),
                AuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().accessToken();
    }

    private <T> ResponseEntity<T> post(final String path, final Object body, final String token, final Class<T> responseType) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
    }

    private <T> ResponseEntity<T> get(final String path, final String token, final Class<T> responseType) {
        final HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    @Test
    void flujoSemanticaInventarioDesconocido_transicionYValidaciones() {
        // 1. Crear producto con cantidadDesconocida = true
        final String codigoBarras = "9900" + System.currentTimeMillis();
        final ProductoCrearRequestDTO crearDto = new ProductoCrearRequestDTO(
                codigoBarras, "Marcador Magico " + codigoBarras, "Marcador permanente",
                1L, 1L, new BigDecimal("10.00"), null, "pieza", new BigDecimal("50.00"), true);

        final ResponseEntity<ProductoDetalleDTO> prodResp = post("/api/v1/productos", crearDto, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.CREATED, prodResp.getStatusCode());
        assertNotNull(prodResp.getBody());
        final Long prodId = prodResp.getBody().id();
        assertTrue(prodResp.getBody().cantidadDesconocida());
        assertEquals(0, prodResp.getBody().stockActual());

        // 2. Realizar venta del producto desconocido (debe pasar sin validar ni alterar stock)
        final VentaRequestDTO ventaDto = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 3)));
        final ResponseEntity<VentaResponseDTO> ventaResp = post("/api/v1/ventas", ventaDto, adminToken, VentaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, ventaResp.getStatusCode());

        // Verificar que el stock sigue en 0 y cantidadDesconocida sigue en true
        final ResponseEntity<ProductoDetalleDTO> prodTrasVenta = get("/api/v1/productos/" + prodId, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, prodTrasVenta.getStatusCode());
        assertTrue(prodTrasVenta.getBody().cantidadDesconocida());
        assertEquals(0, prodTrasVenta.getBody().stockActual());

        // 3. Intento de entrada relativa sobre producto con cantidad desconocida (debe fallar)
        final InventarioMovimientoRequestDTO entradaDto = new InventarioMovimientoRequestDTO(
                prodId, 5, "Entrada invalida", new BigDecimal("10.00"));
        final ResponseEntity<ErrorResponse> entradaResp = post("/api/v1/inventario/entradas", entradaDto, inventaristaToken, ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, entradaResp.getStatusCode());
        assertTrue(entradaResp.getBody().mensaje().contains("cantidad desconocida"));

        // 4. Intento de salida relativa sobre producto con cantidad desconocida (debe fallar)
        final InventarioMovimientoRequestDTO salidaDto = new InventarioMovimientoRequestDTO(
                prodId, 2, "Salida invalida", new BigDecimal("10.00"));
        final ResponseEntity<ErrorResponse> salidaResp = post("/api/v1/inventario/salidas", salidaDto, inventaristaToken, ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, salidaResp.getStatusCode());
        assertTrue(salidaResp.getBody().mensaje().contains("cantidad desconocida"));

        // 5. Ajuste absoluto a 10 (fija el stock a 10 y apaga cantidadDesconocida)
        final AjusteInventarioDTO ajusteAbsoluto = new AjusteInventarioDTO(
                prodId, 10, "Primer conteo inventario", null, true);
        final ResponseEntity<ProductoDetalleDTO> ajusteResp = post("/api/v1/productos/ajustar-inventario", ajusteAbsoluto, inventaristaToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, ajusteResp.getStatusCode());
        assertFalse(ajusteResp.getBody().cantidadDesconocida());
        assertEquals(10, ajusteResp.getBody().stockActual());

        // 6. Venta posterior (ahora con cantidad conocida) debe descontar stock
        final VentaRequestDTO ventaSegunda = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 4)));
        final ResponseEntity<VentaResponseDTO> ventaSegundaResp = post("/api/v1/ventas", ventaSegunda, adminToken, VentaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, ventaSegundaResp.getStatusCode());

        final ResponseEntity<ProductoDetalleDTO> prodTrasSegundaVenta = get("/api/v1/productos/" + prodId, adminToken, ProductoDetalleDTO.class);
        assertEquals(HttpStatus.OK, prodTrasSegundaVenta.getStatusCode());
        assertFalse(prodTrasSegundaVenta.getBody().cantidadDesconocida());
        assertEquals(6, prodTrasSegundaVenta.getBody().stockActual());

        // 7. Venta pidiendo más del stock disponible (6) debe fallar con 400 Bad Request
        final VentaRequestDTO ventaExcesiva = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(prodId, 10)));
        final ResponseEntity<ErrorResponse> ventaExcesivaResp = post("/api/v1/ventas", ventaExcesiva, adminToken, ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, ventaExcesivaResp.getStatusCode());
        assertTrue(ventaExcesivaResp.getBody().mensaje().contains("Stock insuficiente"));
    }

    @Test
    void consultarPerfil_retornaDatosMinimosSegurosPorUsuarioAutenticado() {
        // Admin
        final ResponseEntity<UsuarioPerfilDTO> adminPerfil = get("/api/v1/usuarios/perfil", adminToken, UsuarioPerfilDTO.class);
        assertEquals(HttpStatus.OK, adminPerfil.getStatusCode());
        assertNotNull(adminPerfil.getBody());
        assertEquals("admin@pos.com", adminPerfil.getBody().username());
        assertEquals(RolUsuario.ADMINISTRADOR, adminPerfil.getBody().rol());
        assertNotNull(adminPerfil.getBody().nombre());

        // Inventarista
        final ResponseEntity<UsuarioPerfilDTO> invPerfil = get("/api/v1/usuarios/perfil", inventaristaToken, UsuarioPerfilDTO.class);
        assertEquals(HttpStatus.OK, invPerfil.getStatusCode());
        assertNotNull(invPerfil.getBody());
        assertEquals("inventario.it@pos.com", invPerfil.getBody().username());
        assertEquals(RolUsuario.INVENTARISTA, invPerfil.getBody().rol());
        assertEquals("Luis", invPerfil.getBody().nombre());

        // Sin autenticación
        final ResponseEntity<String> anonPerfil = get("/api/v1/usuarios/perfil", null, String.class);
        assertTrue(anonPerfil.getStatusCode() == HttpStatus.UNAUTHORIZED || anonPerfil.getStatusCode() == HttpStatus.FORBIDDEN);
    }
}

