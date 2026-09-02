package com.kristianconk.api_papeleria.integracion;

import com.kristianconk.api_papeleria.auth.dto.AuthResponse;
import com.kristianconk.api_papeleria.auth.dto.LoginRequest;
import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuento;
import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuentoRepository;
import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuentoResponseDTO;
import com.kristianconk.api_papeleria.autorizacion.SolicitudAutorizacionDescuentoDTO;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.enums.TipoPromocion;
import com.kristianconk.api_papeleria.error.ErrorResponse;
import com.kristianconk.api_papeleria.producto.ProductoDetalleDTO;
import com.kristianconk.api_papeleria.promocion.PromocionRequestDTO;
import com.kristianconk.api_papeleria.promocion.PromocionResponseDTO;
import com.kristianconk.api_papeleria.promocion.ReglaDescuentoPorCantidadDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioCreateRequestDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioResponseDTO;
import com.kristianconk.api_papeleria.utils.TokenOpacoGenerador;
import com.kristianconk.api_papeleria.ventas.DetalleVentaRequestDTO;
import com.kristianconk.api_papeleria.ventas.DetalleVentaResponseDTO;
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
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integración del flujo completo de venta (Tarea 8): backend real,
 * migraciones Flyway reales (V1→V12, ejecutadas de punta a punta en cada corrida) y
 * una base PostgreSQL efímera de Testcontainers — completamente aislada del
 * {@code docker-compose} de desarrollo, nunca lo toca. Cubre atomicidad, concurrencia,
 * expiración, precisión/redondeo, manipulación de requests y conservación histórica,
 * tal como pide el alcance de la Tarea 8.
 *
 * Usa los usuarios y el producto sembrados por {@code V2__datos_prueba.sql}
 * (admin@pos.com, caja@pos.com, producto ID 1 "Cuaderno Profesional 100 hojas" a
 * $37.50) más un GERENTE creado una sola vez para la clase vía el endpoint real de
 * administración.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VentaFlujoIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AutorizacionDescuentoRepository autorizacionDescuentoRepository;

    @Autowired
    private TokenOpacoGenerador tokenOpacoGenerador;

    private String adminToken;
    private String cajaToken;
    private String gerenteToken;

    @BeforeAll
    void autenticarUsuariosSemillaYCrearGerente() {
        adminToken = login("admin@pos.com", "admin123");
        cajaToken = login("caja@pos.com", "caja123");

        // GERENTE no viene en el seed (V2); se crea una sola vez para toda la clase
        // usando el endpoint real de administración, no inserción directa.
        final UsuarioCreateRequestDTO nuevoGerente = new UsuarioCreateRequestDTO(
                "Gerente", "De Prueba", "gerente.it@pos.com", "Gerente123", RolUsuario.GERENTE, 1L);
        final ResponseEntity<UsuarioResponseDTO> creado =
                post("/api/v1/usuarios", nuevoGerente, adminToken, UsuarioResponseDTO.class);
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        gerenteToken = login("gerente.it@pos.com", "Gerente123");
    }

    private String login(final String username, final String password) {
        final ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", new LoginRequest(username, password), AuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody().accessToken();
    }

    private <T> ResponseEntity<T> post(final String path, final Object body, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers(token)), tipo);
    }

    private <T> ResponseEntity<T> put(final String path, final Object body, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers(token)), tipo);
    }

    private <T> ResponseEntity<T> get(final String path, final String token, final Class<T> tipo) {
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers(token)), tipo);
    }

    private HttpHeaders headers(final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    @Test
    void ventaConPromocionPorCantidad_conservaFotografiaHistoricaAunqueSeDesactiveLaPromocion() {
        final PromocionRequestDTO promo = new PromocionRequestDTO(
                "IT - 10% por 10 cuadernos", "prueba de integración", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                1L, null, null, null, 0, new ReglaDescuentoPorCantidadDTO(10, new BigDecimal("10.00")));
        final ResponseEntity<PromocionResponseDTO> promoCreada =
                post("/api/v1/promociones", promo, adminToken, PromocionResponseDTO.class);
        assertEquals(HttpStatus.CREATED, promoCreada.getStatusCode());
        final Long promocionId = promoCreada.getBody().id();

        final VentaRequestDTO ventaRequest = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(1L, 10, null)), null);
        final ResponseEntity<VentaResponseDTO> ventaCreada =
                post("/api/v1/ventas", ventaRequest, cajaToken, VentaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, ventaCreada.getStatusCode());

        final DetalleVentaResponseDTO detalle = ventaCreada.getBody().detalles().getFirst();
        assertEquals(TipoDescuento.CANTIDAD, detalle.tipoDescuento());
        assertEquals(new BigDecimal("10.00"), detalle.porcentajeDescuento());
        assertEquals(new BigDecimal("337.50"), detalle.subtotal()); // 375.00 - 10% = 337.50
        final Long ventaId = ventaCreada.getBody().id();

        // Desactivar la promoción no debe alterar el detalle ya persistido.
        final PromocionRequestDTO promoDesactivada = new PromocionRequestDTO(
                promo.nombre(), promo.descripcion(), promo.tipo(), false, promo.productoId(), promo.categoriaId(),
                promo.fechaInicio(), promo.fechaFin(), promo.prioridad(), promo.reglaDescuentoPorCantidad());
        put("/api/v1/promociones/" + promocionId, promoDesactivada, adminToken, PromocionResponseDTO.class);

        final ResponseEntity<VentaResponseDTO> ventaReconsultada =
                get("/api/v1/ventas/" + ventaId, cajaToken, VentaResponseDTO.class);
        final DetalleVentaResponseDTO detalleReconsultado = ventaReconsultada.getBody().detalles().getFirst();
        assertEquals(TipoDescuento.CANTIDAD, detalleReconsultado.tipoDescuento());
        assertEquals(new BigDecimal("337.50"), detalleReconsultado.subtotal());
    }

    @Test
    void ventaConLineaDeStockInsuficiente_revierteTodoIncluidoElStockYaDescontado() {
        final int stockAntes = get("/api/v1/productos/codigo/7501000110011", cajaToken, ProductoDetalleDTO.class)
                .getBody().stockActual();

        // La primera línea es válida y alcanzaría a descontar stock; la segunda pide
        // una cantidad imposible. La transacción debe revertir ambas.
        final VentaRequestDTO ventaRequest = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(1L, 1, null), new DetalleVentaRequestDTO(9L, 999999, null)),
                null);
        final ResponseEntity<ErrorResponse> respuesta =
                post("/api/v1/ventas", ventaRequest, cajaToken, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().mensaje().contains("Stock insuficiente"));

        final int stockDespues = get("/api/v1/productos/codigo/7501000110011", cajaToken, ProductoDetalleDTO.class)
                .getBody().stockActual();
        assertEquals(stockAntes, stockDespues);
    }

    @Test
    void descuentoManual_soloUnConsumoConcurrenteGanaYLaReutilizacionPosteriorSeRechaza() throws InterruptedException {
        final String carritoId = UUID.randomUUID().toString();
        final SolicitudAutorizacionDescuentoDTO solicitud = new SolicitudAutorizacionDescuentoDTO(
                "gerente.it@pos.com", "Gerente123", 2L, 2, new BigDecimal("15.00"), "prueba IT concurrencia",
                carritoId);
        final ResponseEntity<AutorizacionDescuentoResponseDTO> autorizacionResp = post(
                "/api/v1/autorizaciones-descuento", solicitud, cajaToken, AutorizacionDescuentoResponseDTO.class);
        assertEquals(HttpStatus.CREATED, autorizacionResp.getStatusCode());
        final String referencia = autorizacionResp.getBody().referencia();

        final VentaRequestDTO ventaRequest = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(2L, 2, referencia)), carritoId);

        // Dos hilos intentan confirmar, con la misma referencia, en paralelo: la
        // autorización es de un solo uso, exactamente uno debe ganar.
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch salida = new CountDownLatch(1);
        final AtomicInteger exitosos = new AtomicInteger(0);
        final AtomicInteger rechazados = new AtomicInteger(0);
        final Runnable intentoDeCompra = () -> {
            try {
                salida.await();
                final ResponseEntity<String> resp = restTemplate.exchange(
                        "/api/v1/ventas", HttpMethod.POST, new HttpEntity<>(ventaRequest, headers(cajaToken)),
                        String.class);
                (resp.getStatusCode() == HttpStatus.CREATED ? exitosos : rechazados).incrementAndGet();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        executor.submit(intentoDeCompra);
        executor.submit(intentoDeCompra);
        salida.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(1, exitosos.get());
        assertEquals(1, rechazados.get());

        // Una tercera venta con la misma referencia, ya consumida, también se rechaza.
        final ResponseEntity<ErrorResponse> reutilizacion =
                post("/api/v1/ventas", ventaRequest, cajaToken, ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, reutilizacion.getStatusCode());
    }

    @Test
    void descuentoManual_autorizacionExpirada_seRechazaAlConfirmarLaVenta() {
        final String carritoId = UUID.randomUUID().toString();
        final SolicitudAutorizacionDescuentoDTO solicitud = new SolicitudAutorizacionDescuentoDTO(
                "gerente.it@pos.com", "Gerente123", 1L, 1, new BigDecimal("10.00"), "prueba IT expiración",
                carritoId);
        final ResponseEntity<AutorizacionDescuentoResponseDTO> autorizacionResp = post(
                "/api/v1/autorizaciones-descuento", solicitud, cajaToken, AutorizacionDescuentoResponseDTO.class);
        final String referencia = autorizacionResp.getBody().referencia();

        // Se retrasa el reloj manipulando directamente la fila persistida, en vez de
        // esperar los 2 minutos reales de vigencia. La expiración debe seguir siendo
        // posterior a la emisión (chk_autorizacion_descuento_vigencia de V10) al
        // menos a la precisión de la columna (microsegundos): se fija 1 milisegundo
        // después de emitida, margen suficiente para que ya esté en el pasado cuando
        // la siguiente llamada HTTP (con latencia real) confirme la venta.
        final String hash = tokenOpacoGenerador.hash(referencia);
        final AutorizacionDescuento autorizacion = autorizacionDescuentoRepository.findByTokenHash(hash)
                .orElseThrow();
        autorizacion.setFechaExpiracion(autorizacion.getFechaEmision().plusNanos(1_000_000));
        autorizacionDescuentoRepository.save(autorizacion);

        final VentaRequestDTO ventaRequest = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(1L, 1, referencia)), carritoId);
        final ResponseEntity<ErrorResponse> respuesta =
                post("/api/v1/ventas", ventaRequest, cajaToken, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().mensaje().contains("inválida, expiró"));
    }

    @Test
    void ventaConCamposFabricadosEnElJson_sonIgnoradosYElBackendUsaElPrecioReal() {
        // precioUnitario, porcentajeDescuento y subtotal no existen en
        // DetalleVentaRequestDTO: Jackson los descarta silenciosamente en vez de
        // fallar o de dejarlos afectar el cálculo.
        final String jsonManipulado = """
                {
                  "clienteId": null,
                  "metodoPago": "EFECTIVO",
                  "detalles": [
                    {"productoId": 1, "cantidad": 1, "precioUnitario": 0.01, "porcentajeDescuento": 99, "subtotal": 0.01}
                  ]
                }
                """;
        final ResponseEntity<VentaResponseDTO> respuesta = restTemplate.exchange(
                "/api/v1/ventas", HttpMethod.POST, new HttpEntity<>(jsonManipulado, headers(cajaToken)),
                VentaResponseDTO.class);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        final DetalleVentaResponseDTO detalle = respuesta.getBody().detalles().getFirst();
        assertEquals(new BigDecimal("37.50"), detalle.precioListaUnitario());
        assertEquals(TipoDescuento.NINGUNO, detalle.tipoDescuento());
        assertEquals(new BigDecimal("37.50"), detalle.subtotal());
    }

    @Test
    void promocionPorCantidad_redondeoHalfUpEsExactoYDeterminista() {
        // 4 × $168.00 = $672.00 de subtotal de lista; 33.33% produce un tercer
        // decimal no trivial (672.00 × 33.33 / 100 = 223.9776), buen caso para
        // verificar que el backend redondea HALF_UP a 2 decimales de forma exacta,
        // no con aritmética de punto flotante.
        final PromocionRequestDTO promo = new PromocionRequestDTO(
                "IT - redondeo", "prueba de redondeo", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                7L, null, null, null, 0, new ReglaDescuentoPorCantidadDTO(4, new BigDecimal("33.33")));
        post("/api/v1/promociones", promo, adminToken, PromocionResponseDTO.class);

        final VentaRequestDTO ventaRequest = new VentaRequestDTO(
                null, MetodoPago.EFECTIVO, List.of(new DetalleVentaRequestDTO(7L, 4, null)), null);
        final ResponseEntity<VentaResponseDTO> respuesta =
                post("/api/v1/ventas", ventaRequest, cajaToken, VentaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());

        final BigDecimal subtotalLista = new BigDecimal("672.00");
        final BigDecimal montoEsperado = subtotalLista.multiply(new BigDecimal("33.33"))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        final BigDecimal subtotalEsperado = subtotalLista.subtract(montoEsperado);

        final DetalleVentaResponseDTO detalle = respuesta.getBody().detalles().getFirst();
        assertEquals(0, montoEsperado.compareTo(detalle.montoDescuento()));
        assertEquals(0, subtotalEsperado.compareTo(detalle.subtotal()));
    }
}
