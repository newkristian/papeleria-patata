package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.ClienteRepository;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.cliente.PromocionClienteRepository;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void crearVenta_usaPrecioCatalogoParaDetalleSubtotalYTotal() {
        final VentaRequestDTO request = new VentaRequestDTO(
                null,
                MetodoPago.EFECTIVO,
                List.of(new DetalleVentaRequestDTO(10L, 2)));

        when(folioGenerador.generarFolio()).thenReturn("V-0001");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAnonimo));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
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
        assertEquals(BigDecimal.ZERO, resultado.getDetalles().getFirst().getPorcentajeDescuento());
        assertEquals(BigDecimal.ZERO, resultado.getDetalles().getFirst().getMontoDescuento());
        assertEquals(new BigDecimal("60.00"), resultado.getDetalles().getFirst().getSubtotal());

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
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalStateException.class, () -> ventaService.crearVenta(request, vendedor));

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
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ventaService.crearVenta(request, vendedor);

        assertEquals(new BigDecimal("5050.00"), cliente.getTotalCompras());
        assertEquals("VIP", cliente.getNivel());

        final ArgumentCaptor<PromocionCliente> promocionCaptor = ArgumentCaptor.forClass(PromocionCliente.class);
        verify(promocionClienteRepository).save(promocionCaptor.capture());
        assertEquals(new BigDecimal("10.00"), promocionCaptor.getValue().getPorcentajeDescuento());
    }
}
