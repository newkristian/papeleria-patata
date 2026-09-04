package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuento;
import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuentoService;
import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.ClienteRepository;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.cliente.PromocionClienteRepository;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.promocion.MotorPromocionesService;
import com.kristianconk.api_papeleria.promocion.Promocion;
import com.kristianconk.api_papeleria.promocion.PromocionRepository;
import com.kristianconk.api_papeleria.promocion.ResultadoPromocion;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.utils.FolioGenerador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PromocionClienteRepository promocionClienteRepository;

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private MotorPromocionesService motorPromocionesService;

    @Mock
    private AutorizacionDescuentoService autorizacionDescuentoService;

    @Mock
    private FolioGenerador folioGenerador;

    @InjectMocks
    private VentaService ventaService;

    private Usuario vendedor;
    private Producto producto;
    private Cliente clienteAnonimo;

    @BeforeEach
    void setUp() {
        final Tienda tienda = new Tienda();
        tienda.setId(1L);

        vendedor = new Usuario();
        vendedor.setId(2L);
        vendedor.setRol(RolUsuario.VENDEDOR);
        vendedor.setTienda(tienda);

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Cuaderno profesional");
        producto.setActivo(true);
        producto.setStockActual(20);
        producto.setPrecioVenta(new BigDecimal("30.00"));

        clienteAnonimo = new Cliente();
        clienteAnonimo.setId(1L);
        clienteAnonimo.setNombre("PÚBLICO GENERAL");
    }

    private ResultadoPromocion sinPromocion(final BigDecimal subtotal) {
        final BigDecimal cero = BigDecimal.ZERO.setScale(subtotal.scale());
        return new ResultadoPromocion(TipoDescuento.NINGUNO, null, cero, subtotal, cero, subtotal);
    }

    @Test
    void crearVenta_usaPrecioCatalogoParaDetalleSubtotalYTotal() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2)));

        when(folioGenerador.generarFolio()).thenReturn("V-0001");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(motorPromocionesService.evaluar(eq(producto), eq(2), isNull(), any(LocalDateTime.class)))
                .thenReturn(sinPromocion(new BigDecimal("60.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Venta resultado = ventaService.crearVenta(request, vendedor);

        assertEquals(new BigDecimal("60.00"), resultado.getSubtotal());
        assertEquals(new BigDecimal("0.00"), resultado.getDescuento());
        assertEquals(new BigDecimal("60.00"), resultado.getTotal());
        assertEquals(18, producto.getStockActual());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(new BigDecimal("30.00"), resultado.getDetalles().getFirst().getPrecioListaUnitario());
        assertEquals(new BigDecimal("30.00"), resultado.getDetalles().getFirst().getPrecioUnitarioFinal());
        assertEquals(TipoDescuento.NINGUNO, resultado.getDetalles().getFirst().getTipoDescuento());
        assertEquals(BigDecimal.ZERO.setScale(2), resultado.getDetalles().getFirst().getPorcentajeDescuento());
        assertEquals(BigDecimal.ZERO.setScale(2), resultado.getDetalles().getFirst().getMontoDescuento());
        assertEquals(new BigDecimal("60.00"), resultado.getDetalles().getFirst().getSubtotal());
        assertNull(resultado.getDetalles().getFirst().getPromocionProducto());
        assertNull(resultado.getDetalles().getFirst().getPromocionCliente());

        final ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaRepository).save(ventaCaptor.capture());
        assertEquals(
                new BigDecimal("30.00"),
                ventaCaptor.getValue().getDetalles().getFirst().getPrecioListaUnitario());
    }

    @Test
    void crearVenta_precioCatalogoInvalidoNoModificaInventarioNiPersisteVenta() {
        producto.setPrecioVenta(BigDecimal.ZERO);
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2)));

        when(folioGenerador.generarFolio()).thenReturn("V-0002");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalStateException.class, () -> ventaService.crearVenta(request, vendedor));

        assertEquals(20, producto.getStockActual());
        verify(productoRepository, never()).save(any(Producto.class));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void crearVenta_stockInsuficienteNoModificaInventarioNiPersisteVenta() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 25)));

        when(folioGenerador.generarFolio()).thenReturn("V-0004");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () -> ventaService.crearVenta(request, vendedor));

        assertEquals(20, producto.getStockActual());
        verify(productoRepository, never()).save(any(Producto.class));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void crearVenta_clienteSuperaUmbralAcumulaTotalExactoYCreaPromocionVip() {
        final Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setNombre("Cliente registrado");
        cliente.setNivel("Regular");
        cliente.setTotalCompras(new BigDecimal("4990.00"));

        final VentaRequestDTO request = new VentaRequestDTO(
                2L,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2)));

        when(folioGenerador.generarFolio()).thenReturn("V-0003");
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(motorPromocionesService.evaluar(eq(producto), eq(2), eq(cliente), any(LocalDateTime.class)))
                .thenReturn(sinPromocion(new BigDecimal("60.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ventaService.crearVenta(request, vendedor);

        assertEquals(new BigDecimal("5050.00"), cliente.getTotalCompras());
        assertEquals("VIP", cliente.getNivel());

        final ArgumentCaptor<PromocionCliente> promocionCaptor = ArgumentCaptor.forClass(PromocionCliente.class);
        verify(promocionClienteRepository).save(promocionCaptor.capture());
        assertEquals(new BigDecimal("10.00"), promocionCaptor.getValue().getPorcentajeDescuento());
    }

    @Test
    void crearVenta_ventaAnonimaEvaluaPromocionesSinCliente() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2)));

        when(folioGenerador.generarFolio()).thenReturn("V-0005");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(motorPromocionesService.evaluar(eq(producto), eq(2), isNull(), any(LocalDateTime.class)))
                .thenReturn(sinPromocion(new BigDecimal("60.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ventaService.crearVenta(request, vendedor);

        verify(motorPromocionesService).evaluar(eq(producto), eq(2), isNull(), any(LocalDateTime.class));
    }

    @Test
    void crearVenta_consolidaProductoRepetidoAntesDeEvaluarStockYPromocion() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(
                        new DetalleVentaRequestDTO(10L, 6),
                        new DetalleVentaRequestDTO(10L, 4)));

        when(folioGenerador.generarFolio()).thenReturn("V-0006");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(motorPromocionesService.evaluar(eq(producto), eq(10), isNull(), any(LocalDateTime.class)))
                .thenReturn(sinPromocion(new BigDecimal("300.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Venta resultado = ventaService.crearVenta(request, vendedor);

        assertEquals(1, resultado.getDetalles().size());
        assertEquals(10, resultado.getDetalles().getFirst().getCantidad());
        assertEquals(10, producto.getStockActual());
        verify(motorPromocionesService, times(1))
                .evaluar(eq(producto), eq(10), isNull(), any(LocalDateTime.class));
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void crearVenta_aplicaResultadoDePromocionDeCantidadAlDetalleYAlTotalDeVenta() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 10)));

        final Promocion promocionCantidad = new Promocion();
        promocionCantidad.setId(7L);

        final ResultadoPromocion resultadoPromocion = new ResultadoPromocion(
                TipoDescuento.CANTIDAD, 7L, new BigDecimal("5.00"),
                new BigDecimal("300.00"), new BigDecimal("15.00"), new BigDecimal("285.00"));

        when(folioGenerador.generarFolio()).thenReturn("V-0007");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(motorPromocionesService.evaluar(eq(producto), eq(10), isNull(), any(LocalDateTime.class)))
                .thenReturn(resultadoPromocion);
        when(promocionRepository.getReferenceById(7L)).thenReturn(promocionCantidad);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Venta resultado = ventaService.crearVenta(request, vendedor);

        final DetalleVenta detalle = resultado.getDetalles().getFirst();
        assertEquals(TipoDescuento.CANTIDAD, detalle.getTipoDescuento());
        assertEquals(new BigDecimal("5.00"), detalle.getPorcentajeDescuento());
        assertEquals(new BigDecimal("15.00"), detalle.getMontoDescuento());
        assertEquals(new BigDecimal("285.00"), detalle.getSubtotal());
        assertEquals(new BigDecimal("28.50"), detalle.getPrecioUnitarioFinal());
        assertEquals(promocionCantidad, detalle.getPromocionProducto());
        assertNull(detalle.getPromocionCliente());

        assertEquals(new BigDecimal("300.00"), resultado.getSubtotal());
        assertEquals(new BigDecimal("15.00"), resultado.getDescuento());
        assertEquals(new BigDecimal("285.00"), resultado.getTotal());
    }

    @Test
    void crearVenta_aplicaResultadoDePromocionDeClienteAlDetalle() {
        final Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setNombre("Cliente VIP");
        cliente.setNivel("VIP");
        cliente.setTotalCompras(new BigDecimal("6000.00"));

        final VentaRequestDTO request = new VentaRequestDTO(
                2L,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2)));

        final PromocionCliente promocionCliente = new PromocionCliente();
        promocionCliente.setId(3L);

        final ResultadoPromocion resultadoPromocion = new ResultadoPromocion(
                TipoDescuento.CLIENTE, 3L, new BigDecimal("10.00"),
                new BigDecimal("60.00"), new BigDecimal("6.00"), new BigDecimal("54.00"));

        when(folioGenerador.generarFolio()).thenReturn("V-0008");
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(motorPromocionesService.evaluar(eq(producto), eq(2), eq(cliente), any(LocalDateTime.class)))
                .thenReturn(resultadoPromocion);
        when(promocionClienteRepository.getReferenceById(3L)).thenReturn(promocionCliente);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Venta resultado = ventaService.crearVenta(request, vendedor);

        final DetalleVenta detalle = resultado.getDetalles().getFirst();
        assertEquals(TipoDescuento.CLIENTE, detalle.getTipoDescuento());
        assertEquals(promocionCliente, detalle.getPromocionCliente());
        assertNull(detalle.getPromocionProducto());
        assertEquals(new BigDecimal("54.00"), detalle.getSubtotal());
    }

    private AutorizacionDescuento autorizacionManual(final Usuario autorizador, final BigDecimal porcentaje) {
        final AutorizacionDescuento autorizacion = new AutorizacionDescuento();
        autorizacion.setId(9L);
        autorizacion.setAutorizador(autorizador);
        autorizacion.setVendedor(vendedor);
        autorizacion.setPorcentaje(porcentaje);
        autorizacion.setMotivo("Cliente frecuente, aprobado por gerencia");
        return autorizacion;
    }

    @Test
    void crearVenta_consumeAutorizacionManualYNoInvocaElMotorAutomatico() {
        final Usuario gerente = new Usuario();
        gerente.setId(5L);
        gerente.setRol(RolUsuario.GERENTE);

        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2, "referencia-opaca")),
                "carrito-1");

        when(folioGenerador.generarFolio()).thenReturn("V-0009");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(autorizacionDescuentoService.consumir("referencia-opaca", producto, 2, vendedor, "carrito-1"))
                .thenReturn(autorizacionManual(gerente, new BigDecimal("15.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Venta resultado = ventaService.crearVenta(request, vendedor);

        final DetalleVenta detalle = resultado.getDetalles().getFirst();
        assertEquals(TipoDescuento.MANUAL, detalle.getTipoDescuento());
        assertEquals(new BigDecimal("15.00"), detalle.getPorcentajeDescuento());
        // subtotal lista 60.00, 15% = 9.00 de descuento
        assertEquals(new BigDecimal("9.00"), detalle.getMontoDescuento());
        assertEquals(new BigDecimal("51.00"), detalle.getSubtotal());
        assertEquals(gerente, detalle.getAutorizadoPor());
        assertEquals("Cliente frecuente, aprobado por gerencia", detalle.getMotivoDescuento());
        assertNull(detalle.getPromocionProducto());
        assertNull(detalle.getPromocionCliente());

        verify(motorPromocionesService, never()).evaluar(any(), org.mockito.ArgumentMatchers.anyInt(), any(), any());
    }

    @Test
    void crearVenta_referenciaDeAutorizacionInvalidaPropagaExcepcionYNoPersisteVenta() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2, "referencia-vencida")),
                "carrito-1");

        when(folioGenerador.generarFolio()).thenReturn("V-0010");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(autorizacionDescuentoService.consumir("referencia-vencida", producto, 2, vendedor, "carrito-1"))
                .thenThrow(new IllegalArgumentException("La autorización de descuento manual es inválida, expiró o "
                        + "ya fue utilizada"));

        assertThrows(IllegalArgumentException.class, () -> ventaService.crearVenta(request, vendedor));

        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void crearVenta_dosAutorizacionesDistintasParaElMismoProductoConsolidadoSeRechaza() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(
                        new DetalleVentaRequestDTO(10L, 1, "referencia-a"),
                        new DetalleVentaRequestDTO(10L, 1, "referencia-b")),
                "carrito-1");

        when(folioGenerador.generarFolio()).thenReturn("V-0011");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () -> ventaService.crearVenta(request, vendedor));

        verify(autorizacionDescuentoService, never()).consumir(any(), any(), org.mockito.ArgumentMatchers.anyInt(),
                any(), any());
        verify(motorPromocionesService, never()).evaluar(any(), org.mockito.ArgumentMatchers.anyInt(), any(), any());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    // --- Control de acceso horizontal por tienda (T8) ---

    private Venta ventaDeTienda(final Long tiendaId) {
        final Tienda tienda = new Tienda();
        tienda.setId(tiendaId);
        final Venta venta = new Venta();
        venta.setId(100L);
        venta.setTienda(tienda);
        return venta;
    }

    @Test
    void getAllVentas_administrador_veTodasLasTiendas() {
        final Usuario admin = new Usuario();
        admin.setRol(RolUsuario.ADMINISTRADOR);
        when(ventaRepository.findAll()).thenReturn(List.of(ventaDeTienda(1L), ventaDeTienda(2L)));

        final List<Venta> resultado = ventaService.getAllVentas(admin);

        assertEquals(2, resultado.size());
        verify(ventaRepository, never()).findByTiendaId(any());
    }

    @Test
    void getAllVentas_vendedor_soloVeSuPropiaTienda() {
        when(ventaRepository.findByTiendaId(1L)).thenReturn(List.of(ventaDeTienda(1L)));

        final List<Venta> resultado = ventaService.getAllVentas(vendedor);

        assertEquals(1, resultado.size());
        verify(ventaRepository).findByTiendaId(1L);
        verify(ventaRepository, never()).findAll();
    }

    @Test
    void getVentaById_administrador_puedeVerVentaDeCualquierTienda() {
        final Usuario admin = new Usuario();
        admin.setRol(RolUsuario.ADMINISTRADOR);
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(ventaDeTienda(99L)));

        final Venta resultado = ventaService.getVentaById(100L, admin);

        assertEquals(99L, resultado.getTienda().getId());
    }

    @Test
    void getVentaById_vendedorDeOtraTienda_recibe404ComoSiNoExistiera() {
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(ventaDeTienda(99L)));

        assertThrows(com.kristianconk.api_papeleria.error.ResourceNotFoundException.class,
                () -> ventaService.getVentaById(100L, vendedor));
    }

    @Test
    void getVentaById_vendedorDeLaMismaTienda_laPuedeVer() {
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(ventaDeTienda(1L)));

        final Venta resultado = ventaService.getVentaById(100L, vendedor);

        assertEquals(1L, resultado.getTienda().getId());
    }

    @Test
    void getVentasPorCliente_vendedor_filtraSoloLasDeSuTienda() {
        when(ventaRepository.findByClienteId(5L)).thenReturn(List.of(ventaDeTienda(1L), ventaDeTienda(2L)));

        final List<Venta> resultado = ventaService.getVentasPorCliente(5L, vendedor);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.getFirst().getTienda().getId());
    }

    @Test
    void getVentasPorCliente_administrador_veTodasLasTiendas() {
        final Usuario admin = new Usuario();
        admin.setRol(RolUsuario.ADMINISTRADOR);
        when(ventaRepository.findByClienteId(5L)).thenReturn(List.of(ventaDeTienda(1L), ventaDeTienda(2L)));

        final List<Venta> resultado = ventaService.getVentasPorCliente(5L, admin);

        assertEquals(2, resultado.size());
    }

    @Test
    void crearVenta_productoCantidadDesconocida_noValidaNiDescuentaStock() {
        final Producto productoDesconocido = new Producto();
        productoDesconocido.setId(20L);
        productoDesconocido.setNombre("Producto Sin Inventariar");
        productoDesconocido.setActivo(true);
        productoDesconocido.setStockActual(0); // Stock en 0, pero cantidad desconocida = true
        productoDesconocido.setCantidadDesconocida(true);
        productoDesconocido.setPrecioVenta(new BigDecimal("15.00"));

        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(20L, 5)));

        when(folioGenerador.generarFolio()).thenReturn("V-0020");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(productoDesconocido));
        when(motorPromocionesService.evaluar(eq(productoDesconocido), eq(5), isNull(), any(LocalDateTime.class)))
                .thenReturn(sinPromocion(new BigDecimal("75.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Venta resultado = ventaService.crearVenta(request, vendedor);

        assertNotNull(resultado);
        assertEquals(0, productoDesconocido.getStockActual()); // Stock sigue en 0 sin modificar
        verify(productoRepository, never()).save(productoDesconocido);
    }
}

