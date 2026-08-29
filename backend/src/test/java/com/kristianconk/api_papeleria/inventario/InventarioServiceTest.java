package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Usuario usuarioInventarista;
    private Usuario usuarioVendedor;
    private Producto productoMock;

    @BeforeEach
    void setUp() {
        usuarioInventarista = new Usuario();
        usuarioInventarista.setId(1L);
        usuarioInventarista.setNombre("Luis Inventarios");
        usuarioInventarista.setRol(RolUsuario.INVENTARISTA);

        usuarioVendedor = new Usuario();
        usuarioVendedor.setId(2L);
        usuarioVendedor.setNombre("Ana Ventas");
        usuarioVendedor.setRol(RolUsuario.VENDEDOR);

        productoMock = new Producto();
        productoMock.setId(10L);
        productoMock.setNombre("Cuaderno Profesional");
        productoMock.setCodigoBarras("750103504821");
        productoMock.setStockActual(5);
        productoMock.setCostoCompra(new BigDecimal("20.00"));
        productoMock.setPorcentajeGanancia(new BigDecimal("50.00"));
        productoMock.setActivo(true);
    }

    @Test
    void registrarEntrada_aumentoCosto_actualizaProducto() {
        // Given
        final InventarioMovimientoRequestDTO request = new InventarioMovimientoRequestDTO(
                10L, 10, "Compra proveedores julio", new BigDecimal("25.00"));

        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoMock));
        when(inventarioMovimientoRepository.save(any(InventarioMovimiento.class))).thenAnswer(invocation -> {
            final InventarioMovimiento mov = invocation.getArgument(0);
            mov.setId(100L);
            return mov;
        });

        // When
        final InventarioMovimiento resultado = inventarioService.registrarEntrada(request, usuarioInventarista);

        // Then
        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals(TipoMovimiento.ENTRADA, resultado.getTipo());
        assertEquals(10, resultado.getCantidad());
        assertEquals(new BigDecimal("25.00"), resultado.getCostoUnitario());

        // Verificamos que el stock subió: 5 + 10 = 15
        assertEquals(15, productoMock.getStockActual());
        // Verificamos que el costo subió: de 20.0 a 25.0
        assertEquals(new BigDecimal("25.00"), productoMock.getCostoCompra());
        // 25.0 < 50.0 -> porcentaje de ganancia debe ser 50.0
        assertEquals(new BigDecimal("50.00"), productoMock.getPorcentajeGanancia());

        verify(productoRepository).save(productoMock);
        verify(inventarioMovimientoRepository).save(any(InventarioMovimiento.class));
    }

    @Test
    void registrarEntrada_disminucionCosto_noActualizaProducto() {
        // Given
        final InventarioMovimientoRequestDTO request = new InventarioMovimientoRequestDTO(
                10L, 15, "Compra barata liquidación", new BigDecimal("18.00"));

        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoMock));
        when(inventarioMovimientoRepository.save(any(InventarioMovimiento.class))).thenAnswer(invocation -> {
            final InventarioMovimiento mov = invocation.getArgument(0);
            mov.setId(101L);
            return mov;
        });

        // When
        final InventarioMovimiento resultado = inventarioService.registrarEntrada(request, usuarioInventarista);

        // Then
        assertNotNull(resultado);
        assertEquals(101L, resultado.getId());
        // El movimiento de inventario guarda el costo real pagado (18.0)
        assertEquals(new BigDecimal("18.00"), resultado.getCostoUnitario());

        // El stock subió: 5 + 15 = 20
        assertEquals(20, productoMock.getStockActual());
        // El costo de catálogo NO se reduce (sigue en 20.0)
        assertEquals(new BigDecimal("20.00"), productoMock.getCostoCompra());

        verify(productoRepository).save(productoMock);
        verify(inventarioMovimientoRepository).save(any(InventarioMovimiento.class));
    }

    @Test
    void registrarEntrada_usuarioSinPermisos_lanzaExcepcion() {
        // Given
        final InventarioMovimientoRequestDTO request = new InventarioMovimientoRequestDTO(
                10L, 10, "Compra proveedores", new BigDecimal("25.00"));

        // When & Then
        assertThrows(AccesoDenegadoException.class, () -> 
                inventarioService.registrarEntrada(request, usuarioVendedor));

        verifyNoInteractions(productoRepository);
        verifyNoInteractions(inventarioMovimientoRepository);
    }

    @Test
    void registrarSalida_stockSuficiente_descuentaStock() {
        // Given
        final InventarioMovimientoRequestDTO request = new InventarioMovimientoRequestDTO(
                10L, 3, "Merma por daño", null);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoMock));
        when(inventarioMovimientoRepository.save(any(InventarioMovimiento.class))).thenAnswer(invocation -> {
            final InventarioMovimiento mov = invocation.getArgument(0);
            mov.setId(102L);
            return mov;
        });

        // When
        final InventarioMovimiento resultado = inventarioService.registrarSalida(request, usuarioInventarista);

        // Then
        assertNotNull(resultado);
        assertEquals(102L, resultado.getId());
        assertEquals(TipoMovimiento.SALIDA, resultado.getTipo());
        assertEquals(3, resultado.getCantidad());
        // Si no se pasa costo unitario en salida, usa el del producto
        assertEquals(new BigDecimal("20.00"), resultado.getCostoUnitario());

        // Stock bajó: 5 - 3 = 2
        assertEquals(2, productoMock.getStockActual());

        verify(productoRepository).save(productoMock);
        verify(inventarioMovimientoRepository).save(any(InventarioMovimiento.class));
    }

    @Test
    void registrarSalida_stockInsuficiente_lanzaExcepcion() {
        // Given
        final InventarioMovimientoRequestDTO request = new InventarioMovimientoRequestDTO(
                10L, 6, "Merma por daño", null);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoMock));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                inventarioService.registrarSalida(request, usuarioInventarista));

        assertEquals(5, productoMock.getStockActual()); // Stock sigue igual
        verify(productoRepository, never()).save(any(Producto.class));
        verifyNoInteractions(inventarioMovimientoRepository);
    }
}
