package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.categoria.CategoriaRepository;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ConflictException;
import com.kristianconk.api_papeleria.inventario.AjusteInventarioDTO;
import com.kristianconk.api_papeleria.inventario.InventarioMovimientoRepository;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import com.kristianconk.api_papeleria.proveedor.ProveedorPendienteService;
import com.kristianconk.api_papeleria.proveedor.ProveedorRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProveedorPendienteService proveedorPendienteService;

    @Mock
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    private ProductoService productoService;
    private Categoria categoria;
    private Proveedor proveedor;
    private Proveedor pendiente;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(
                productoRepository,
                categoriaRepository,
                proveedorRepository,
                proveedorPendienteService,
                inventarioMovimientoRepository);
        categoria = categoria(1L, "Escolar");
        proveedor = proveedor(2L, "Distribuidora", true, false);
        pendiente = proveedor(3L, "PENDIENTE", true, true);
    }

    @Test
    void crearSinProveedorAsignaPendienteYConservaRelacionObligatoria() {
        final ProductoCrearRequestDTO request = crearRequest(null, null);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(proveedorPendienteService.obtener()).thenReturn(pendiente);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            final Producto producto = invocation.getArgument(0);
            producto.setId(10L);
            return producto;
        });

        final ProductoDetalleDTO resultado = productoService.crearProducto(
                request, usuario(RolUsuario.INVENTARISTA));

        final ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertSame(pendiente, captor.getValue().getProveedor());
        assertTrue(captor.getValue().isCantidadDesconocida());
        assertTrue(captor.getValue().isActivo());
        assertEquals(0, captor.getValue().getStockActual());
        assertEquals("PENDIENTE", resultado.proveedor().nombre());
    }

    @Test
    void crearConProveedorActivoLoAsigna() {
        final ProductoCrearRequestDTO request = crearRequest(2L, false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(proveedorRepository.findById(2L)).thenReturn(Optional.of(proveedor));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final ProductoDetalleDTO resultado = productoService.crearProducto(
                request, usuario(RolUsuario.ADMINISTRADOR));

        assertEquals("Distribuidora", resultado.proveedor().nombre());
        assertFalse(resultado.cantidadDesconocida());
        verifyNoInteractions(proveedorPendienteService);
    }

    @Test
    void crearRechazaCodigoDuplicadoConConflicto() {
        final ProductoCrearRequestDTO request = crearRequest(null, true);
        when(productoRepository.existsByCodigoBarrasIgnoreCase("SKU-001")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> productoService.crearProducto(request, usuario(RolUsuario.ADMINISTRADOR)));

        verifyNoInteractions(categoriaRepository, proveedorRepository, proveedorPendienteService);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crearRechazaProveedorInactivo() {
        proveedor.setActivo(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(proveedorRepository.findById(2L)).thenReturn(Optional.of(proveedor));

        assertThrows(ConflictException.class,
                () -> productoService.crearProducto(crearRequest(2L, true), usuario(RolUsuario.ADMINISTRADOR)));
    }

    @Test
    void actualizarConProveedorNuloVuelveAPendienteSinAlterarStockNiEstado() {
        final Producto producto = producto(10L, proveedor);
        producto.setStockActual(27);
        producto.setActivo(false);
        producto.setCantidadDesconocida(false);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(proveedorPendienteService.obtener()).thenReturn(pendiente);
        when(productoRepository.save(producto)).thenReturn(producto);

        final ProductoDetalleDTO resultado = productoService.actualizarProducto(
                10L, actualizarRequest(null), usuario(RolUsuario.INVENTARISTA));

        assertSame(pendiente, producto.getProveedor());
        assertEquals(27, producto.getStockActual());
        assertFalse(producto.isActivo());
        assertFalse(producto.isCantidadDesconocida());
        assertEquals("PENDIENTE", resultado.proveedor().nombre());
    }

    @Test
    void actualizarRechazaCodigoDeOtroProducto() {
        final Producto producto = producto(10L, proveedor);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsByCodigoBarrasIgnoreCaseAndIdNot("SKU-002", 10L)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> productoService.actualizarProducto(
                        10L, actualizarRequestConCodigo(2L, "SKU-002"), usuario(RolUsuario.GERENTE)));

        verify(productoRepository, never()).save(producto);
    }

    @Test
    void inventaristaNoPuedeFijarMargenManual() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(proveedorRepository.findById(2L)).thenReturn(Optional.of(proveedor));

        assertThrows(AccesoDenegadoException.class,
                () -> productoService.crearProducto(
                        crearRequestConMargen(2L, new BigDecimal("20.00")),
                        usuario(RolUsuario.INVENTARISTA)));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void administradorDesactivaYGerenteReactiva() {
        final Producto producto = producto(10L, proveedor);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        final ProductoDetalleDTO desactivado =
                productoService.desactivarProducto(10L, usuario(RolUsuario.ADMINISTRADOR));
        assertFalse(desactivado.activo());

        final ProductoDetalleDTO reactivado =
                productoService.reactivarProducto(10L, usuario(RolUsuario.GERENTE));
        assertTrue(reactivado.activo());
    }

    @Test
    void inventaristaNoPuedeCambiarEstado() {
        assertThrows(AccesoDenegadoException.class,
                () -> productoService.desactivarProducto(10L, usuario(RolUsuario.INVENTARISTA)));

        verifyNoInteractions(productoRepository);
    }

    @Test
    void busquedaDelVendedorFuerzaSoloActivosAunqueSoliciteInactivos() {
        final PageRequest pageable = PageRequest.of(0, 20);
        final ProductoBusquedaDTO busqueda =
                new ProductoBusquedaDTO(" lápiz ", null, null, false, null, null, false);
        when(productoRepository.buscarProductos("%lápiz%", null, null, true, null, null, false, pageable))
                .thenReturn(new PageImpl<>(List.of(producto(10L, proveedor)), pageable, 1));

        productoService.buscarProductos(busqueda, pageable, usuario(RolUsuario.VENDEDOR));

        verify(productoRepository).buscarProductos(
                "%lápiz%", null, null, true, null, null, false, pageable);
    }

    @Test
    void busquedaAdministrativaSinFiltroMantieneSoloActivosPorDefecto() {
        final PageRequest pageable = PageRequest.of(0, 20);
        final ProductoBusquedaDTO busqueda =
                new ProductoBusquedaDTO(null, null, null, null, null, null, false);
        when(productoRepository.buscarProductos(null, null, null, true, null, null, false, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        productoService.buscarProductos(busqueda, pageable, usuario(RolUsuario.INVENTARISTA));

        verify(productoRepository).buscarProductos(
                null, null, null, true, null, null, false, pageable);
    }

    @Test
    void ajustarInventario_absolutoCero_fijaStockYApagaCantidadDesconocida() {
        final Producto producto = producto(10L, proveedor);
        producto.setCantidadDesconocida(true);
        producto.setStockActual(0);

        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        final AjusteInventarioDTO ajuste = new AjusteInventarioDTO(10L, 0, "Conteo inicial cero", null, true);
        final ProductoDetalleDTO resultado = productoService.ajustarInventario(ajuste, usuario(RolUsuario.INVENTARISTA));

        assertNotNull(resultado);
        assertFalse(producto.isCantidadDesconocida());
        assertEquals(0, producto.getStockActual());
        verify(inventarioMovimientoRepository).save(any());
    }

    @Test
    void ajustarInventario_absolutoNegativo_lanzaExcepcion() {
        final Producto producto = producto(10L, proveedor);
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        final AjusteInventarioDTO ajuste = new AjusteInventarioDTO(10L, -5, "Ajuste negativo invalido", null, true);
        assertThrows(IllegalArgumentException.class, () -> productoService.ajustarInventario(ajuste, usuario(RolUsuario.INVENTARISTA)));
    }

    @Test
    void ajustarInventario_relativoConCantidadDesconocida_lanzaExcepcion() {
        final Producto producto = producto(10L, proveedor);
        producto.setCantidadDesconocida(true);
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        final AjusteInventarioDTO ajuste = new AjusteInventarioDTO(10L, 5, "Ajuste relativo invalido", null, false);
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.ajustarInventario(ajuste, usuario(RolUsuario.INVENTARISTA)));

        assertTrue(ex.getMessage().contains("cantidad desconocida"));
    }

    @Test
    void ajustarInventario_relativoResultaEnStockNegativo_lanzaExcepcion() {
        final Producto producto = producto(10L, proveedor);
        producto.setStockActual(3);
        producto.setCantidadDesconocida(false);
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(producto));

        final AjusteInventarioDTO ajuste = new AjusteInventarioDTO(10L, -5, "Ajuste resulta en negativo", null, false);
        assertThrows(IllegalArgumentException.class, () -> productoService.ajustarInventario(ajuste, usuario(RolUsuario.INVENTARISTA)));
    }

    private ProductoCrearRequestDTO crearRequest(final Long proveedorId, final Boolean cantidadDesconocida) {
        return crearRequestConMargen(proveedorId, null, cantidadDesconocida);
    }

    private ProductoCrearRequestDTO crearRequestConMargen(
            final Long proveedorId,
            final BigDecimal margen) {
        return crearRequestConMargen(proveedorId, margen, true);
    }

    private ProductoCrearRequestDTO crearRequestConMargen(
            final Long proveedorId,
            final BigDecimal margen,
            final Boolean cantidadDesconocida) {
        return new ProductoCrearRequestDTO(
                " SKU-001 ", " Cuaderno ", " descripción ", 1L, proveedorId,
                new BigDecimal("10.00"), null, " pieza ", margen, cantidadDesconocida);
    }

    private ProductoActualizarRequestDTO actualizarRequest(final Long proveedorId) {
        return actualizarRequestConCodigo(proveedorId, "SKU-001");
    }

    private ProductoActualizarRequestDTO actualizarRequestConCodigo(
            final Long proveedorId,
            final String codigo) {
        return new ProductoActualizarRequestDTO(
                codigo, "Cuaderno actualizado", null, 1L, proveedorId,
                new BigDecimal("12.00"), 3, "pieza", null);
    }

    private Producto producto(final Long id, final Proveedor proveedorProducto) {
        final Producto producto = new Producto();
        producto.setId(id);
        producto.setCodigoBarras("SKU-001");
        producto.setNombre("Cuaderno");
        producto.setCategoria(categoria);
        producto.setProveedor(proveedorProducto);
        producto.setCostoCompra(new BigDecimal("10.00"));
        producto.setPorcentajeGanancia(new BigDecimal("50.00"));
        producto.setPrecioVenta(new BigDecimal("15.00"));
        producto.setStockMinimo(5);
        producto.setStockActual(10);
        producto.setUnidadMedida("pieza");
        producto.setActivo(true);
        return producto;
    }

    private Categoria categoria(final Long id, final String nombre) {
        final Categoria resultado = new Categoria();
        resultado.setId(id);
        resultado.setNombre(nombre);
        return resultado;
    }

    private Proveedor proveedor(
            final Long id,
            final String nombre,
            final boolean activo,
            final boolean sistema) {
        final Proveedor resultado = new Proveedor();
        resultado.setId(id);
        resultado.setNombre(nombre);
        resultado.setPorcentajeComision(BigDecimal.ZERO);
        resultado.setActivo(activo);
        resultado.setSistema(sistema);
        return resultado;
    }

    private Usuario usuario(final RolUsuario rol) {
        final Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol(rol);
        return usuario;
    }
}
