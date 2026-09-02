package com.kristianconk.api_papeleria.autorizacion;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.promocion.MotorPromocionesService;
import com.kristianconk.api_papeleria.promocion.ResultadoPromocion;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.usuario.UsuarioRepository;
import com.kristianconk.api_papeleria.utils.TokenOpacoGenerador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorizacionDescuentoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private AutorizacionDescuentoRepository autorizacionDescuentoRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MotorPromocionesService motorPromocionesService;

    @Mock
    private TokenOpacoGenerador tokenOpacoGenerador;

    @Mock
    private LimitadorIntentosAutorizacion limitadorIntentosAutorizacion;

    @InjectMocks
    private AutorizacionDescuentoService autorizacionDescuentoService;

    private Tienda tienda;
    private Usuario vendedor;
    private Usuario gerente;
    private Usuario administrador;
    private Producto producto;

    @BeforeEach
    void setUp() {
        tienda = new Tienda();
        tienda.setId(1L);

        vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setUsername("caja@pos.com");
        vendedor.setRol(RolUsuario.VENDEDOR);
        vendedor.setTienda(tienda);

        gerente = new Usuario();
        gerente.setId(3L);
        gerente.setUsername("gerente@pos.com");
        gerente.setRol(RolUsuario.GERENTE);
        gerente.setTienda(tienda);
        gerente.setActivo(true);

        administrador = new Usuario();
        administrador.setId(4L);
        administrador.setUsername("admin@pos.com");
        administrador.setRol(RolUsuario.ADMINISTRADOR);
        administrador.setActivo(true);

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Impresora láser");
        producto.setActivo(true);
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setCostoCompra(new BigDecimal("80.00"));
    }

    private SolicitudAutorizacionDescuentoDTO solicitud(final String username, final BigDecimal porcentaje) {
        return new SolicitudAutorizacionDescuentoDTO(
                username, "clave-cualquiera", 10L, 2, porcentaje, "Cliente frecuente", "carrito-1");
    }

    @Test
    void solicitar_credencialesInvalidas_propagaAuthenticationExceptionYRegistraFallo() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mala clave"));

        assertThrows(BadCredentialsException.class,
                () -> autorizacionDescuentoService.solicitar(solicitud("gerente@pos.com", new BigDecimal("10.00")), vendedor));

        verify(limitadorIntentosAutorizacion).registrarFallo(vendedor.getId());
        verify(usuarioRepository, never()).findByUsername(any());
    }

    @Test
    void solicitar_limiteDeIntentosBloqueado_noIntentaAutenticar() {
        org.mockito.Mockito.doThrow(new com.kristianconk.api_papeleria.error.AccesoDenegadoException("bloqueado"))
                .when(limitadorIntentosAutorizacion).verificarDisponible(vendedor.getId());

        assertThrows(com.kristianconk.api_papeleria.error.AccesoDenegadoException.class,
                () -> autorizacionDescuentoService.solicitar(solicitud("gerente@pos.com", new BigDecimal("10.00")), vendedor));

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void solicitar_rolNoPermitido_lanzaAccesoDenegado() {
        when(usuarioRepository.findByUsername("caja@pos.com")).thenReturn(Optional.of(vendedor));

        assertThrows(com.kristianconk.api_papeleria.error.AccesoDenegadoException.class,
                () -> autorizacionDescuentoService.solicitar(solicitud("caja@pos.com", new BigDecimal("10.00")), vendedor));

        verify(limitadorIntentosAutorizacion).registrarFallo(vendedor.getId());
        verify(autorizacionDescuentoRepository, never()).save(any());
    }

    @Test
    void solicitar_gerenteDeOtraTienda_lanzaAccesoDenegado() {
        final Tienda otraTienda = new Tienda();
        otraTienda.setId(99L);
        gerente.setTienda(otraTienda);
        when(usuarioRepository.findByUsername("gerente@pos.com")).thenReturn(Optional.of(gerente));

        assertThrows(com.kristianconk.api_papeleria.error.AccesoDenegadoException.class,
                () -> autorizacionDescuentoService.solicitar(solicitud("gerente@pos.com", new BigDecimal("10.00")), vendedor));

        verify(autorizacionDescuentoRepository, never()).save(any());
    }

    @Test
    void solicitar_gerenteBajoCosto_lanzaIllegalArgumentException() {
        // 30% de 100.00 = 70.00, por debajo del costo (80.00)
        when(usuarioRepository.findByUsername("gerente@pos.com")).thenReturn(Optional.of(gerente));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class,
                () -> autorizacionDescuentoService.solicitar(solicitud("gerente@pos.com", new BigDecimal("30.00")), vendedor));

        verify(autorizacionDescuentoRepository, never()).save(any());
    }

    @Test
    void solicitar_administradorBajoCosto_sePermite() {
        when(usuarioRepository.findByUsername("admin@pos.com")).thenReturn(Optional.of(administrador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(tokenOpacoGenerador.generar()).thenReturn("token-plano");
        when(tokenOpacoGenerador.hash("token-plano")).thenReturn("hash-token");
        when(motorPromocionesService.evaluar(any(), org.mockito.ArgumentMatchers.eq(2), any(), any()))
                .thenReturn(sinPromocion());

        final AutorizacionDescuentoResponseDTO respuesta = autorizacionDescuentoService.solicitar(
                solicitud("admin@pos.com", new BigDecimal("30.00")), vendedor);

        assertEquals("token-plano", respuesta.referencia());
        verify(autorizacionDescuentoRepository).save(any(AutorizacionDescuento.class));
    }

    @Test
    void solicitar_exitoso_guardaAutorizacionConHashYLimpiaIntentos() {
        when(usuarioRepository.findByUsername("gerente@pos.com")).thenReturn(Optional.of(gerente));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(tokenOpacoGenerador.generar()).thenReturn("token-plano");
        when(tokenOpacoGenerador.hash("token-plano")).thenReturn("hash-token");
        when(motorPromocionesService.evaluar(any(), org.mockito.ArgumentMatchers.eq(2), any(), any()))
                .thenReturn(sinPromocion());

        final AutorizacionDescuentoResponseDTO respuesta = autorizacionDescuentoService.solicitar(
                solicitud("gerente@pos.com", new BigDecimal("10.00")), vendedor);

        assertEquals("token-plano", respuesta.referencia());
        assertEquals(new BigDecimal("10.00"), respuesta.porcentaje());
        assertEquals(new BigDecimal("90.00"), respuesta.precioFinalEstimado());
        assertEquals(new BigDecimal("20.00"), respuesta.montoDescuentoEstimado());
        assertTrue(respuesta.expiraEn().isAfter(LocalDateTime.now()));

        final ArgumentCaptor<AutorizacionDescuento> captor = ArgumentCaptor.forClass(AutorizacionDescuento.class);
        verify(autorizacionDescuentoRepository).save(captor.capture());
        assertEquals("hash-token", captor.getValue().getTokenHash());
        assertEquals(gerente, captor.getValue().getAutorizador());
        assertEquals(vendedor, captor.getValue().getVendedor());
        assertEquals("carrito-1", captor.getValue().getCarritoId());
        // T8: fotografía de auditoría — rol del autorizador y costo considerado en
        // este momento, independientes de lo que Usuario/Producto digan después.
        assertEquals(RolUsuario.GERENTE, captor.getValue().getRolAutorizador());
        assertEquals(new BigDecimal("80.00"), captor.getValue().getCostoConsiderado());
        assertEquals(BigDecimal.ZERO.setScale(2), captor.getValue().getMontoPromocionAutomaticaDisponible());

        verify(limitadorIntentosAutorizacion).registrarExito(vendedor.getId());
    }

    @Test
    void solicitar_conPromocionAutomaticaDisponible_laConservaParaAuditoriaYComparacion() {
        when(usuarioRepository.findByUsername("gerente@pos.com")).thenReturn(Optional.of(gerente));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(tokenOpacoGenerador.generar()).thenReturn("token-plano");
        when(tokenOpacoGenerador.hash("token-plano")).thenReturn("hash-token");
        final ResultadoPromocion conPromocion = new ResultadoPromocion(
                TipoDescuento.CANTIDAD, 5L, new BigDecimal("5.00"), new BigDecimal("200.00"),
                new BigDecimal("15.00"), new BigDecimal("185.00"));
        when(motorPromocionesService.evaluar(any(), org.mockito.ArgumentMatchers.eq(2), any(), any()))
                .thenReturn(conPromocion);

        final AutorizacionDescuentoResponseDTO respuesta = autorizacionDescuentoService.solicitar(
                solicitud("gerente@pos.com", new BigDecimal("10.00")), vendedor);

        assertTrue(respuesta.promocionAutomaticaDisponible());
        assertEquals(new BigDecimal("15.00"), respuesta.montoPromocionAutomatica());
        // 20.00 (manual) - 15.00 (automática) = 5.00 de diferencia a favor del manual.
        assertEquals(new BigDecimal("5.00"), respuesta.diferenciaVsPromocionAutomatica());

        final ArgumentCaptor<AutorizacionDescuento> captor = ArgumentCaptor.forClass(AutorizacionDescuento.class);
        verify(autorizacionDescuentoRepository).save(captor.capture());
        assertEquals(new BigDecimal("15.00"), captor.getValue().getMontoPromocionAutomaticaDisponible());
    }

    private ResultadoPromocion sinPromocion() {
        final BigDecimal cero = BigDecimal.ZERO.setScale(2);
        return new ResultadoPromocion(TipoDescuento.NINGUNO, null, cero, new BigDecimal("200.00"), cero,
                new BigDecimal("200.00"));
    }

    private AutorizacionDescuento autorizacionVigente() {
        final AutorizacionDescuento autorizacion = new AutorizacionDescuento();
        autorizacion.setId(1L);
        autorizacion.setTokenHash("hash-token");
        autorizacion.setAutorizador(gerente);
        autorizacion.setVendedor(vendedor);
        autorizacion.setTienda(tienda);
        autorizacion.setProducto(producto);
        autorizacion.setCantidad(2);
        autorizacion.setPorcentaje(new BigDecimal("10.00"));
        autorizacion.setMotivo("Cliente frecuente");
        autorizacion.setCarritoId("carrito-1");
        autorizacion.setFechaEmision(LocalDateTime.now().minusSeconds(30));
        autorizacion.setFechaExpiracion(LocalDateTime.now().plusMinutes(1));
        autorizacion.setConsumida(false);
        return autorizacion;
    }

    @Test
    void consumir_referenciaInexistente_lanzaIllegalArgumentException() {
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-inexistente");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-1"));
    }

    @Test
    void consumir_autorizacionExpirada_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        autorizacion.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-1"));

        verify(autorizacionDescuentoRepository, never()).consumirSiVigente(anyLong(), any());
    }

    @Test
    void consumir_autorizacionYaConsumida_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        autorizacion.setConsumida(true);
        autorizacion.setFechaConsumo(LocalDateTime.now().minusSeconds(10));
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-1"));
    }

    @Test
    void consumir_productoNoCoincide_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        final Producto otroProducto = new Producto();
        otroProducto.setId(99L);
        otroProducto.setPrecioVenta(new BigDecimal("50.00"));
        otroProducto.setCostoCompra(new BigDecimal("10.00"));
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", otroProducto, 2, vendedor, "carrito-1"));
    }

    @Test
    void consumir_cantidadNoCoincide_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 5, vendedor, "carrito-1"));
    }

    @Test
    void consumir_vendedorNoCoincide_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        final Usuario otroVendedor = new Usuario();
        otroVendedor.setId(55L);
        otroVendedor.setTienda(tienda);
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, otroVendedor, "carrito-1"));
    }

    @Test
    void consumir_carritoNoCoincide_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-distinto"));
    }

    @Test
    void consumir_costoSubioEntreEmisionYConsumo_lanzaIllegalArgumentException() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));
        // 10% de 100.00 = 90.00 final; si el costo subió a 95.00, un GERENTE ya no
        // puede autorizarlo aunque el porcentaje siga siendo el mismo.
        producto.setCostoCompra(new BigDecimal("95.00"));

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-1"));

        verify(autorizacionDescuentoRepository, never()).consumirSiVigente(anyLong(), any());
    }

    @Test
    void consumir_dosConsumosConcurrentes_soloUnoGana() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));
        when(autorizacionDescuentoRepository.consumirSiVigente(org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-1"));
    }

    @Test
    void consumir_exitoso_marcaConsumidaYRetornaAutorizacion() {
        final AutorizacionDescuento autorizacion = autorizacionVigente();
        when(tokenOpacoGenerador.hash("referencia")).thenReturn("hash-token");
        when(autorizacionDescuentoRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(autorizacion));
        when(autorizacionDescuentoRepository.consumirSiVigente(org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenReturn(1);

        final AutorizacionDescuento resultado = autorizacionDescuentoService.consumir(
                "referencia", producto, 2, vendedor, "carrito-1");

        assertTrue(resultado.isConsumida());
        assertEquals(gerente, resultado.getAutorizador());
        assertEquals(new BigDecimal("10.00"), resultado.getPorcentaje());
    }
}
