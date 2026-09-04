package com.kristianconk.api_papeleria.proveedor;

import com.kristianconk.api_papeleria.error.ConflictException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorPendienteService proveedorPendienteService;

    private ProveedorService proveedorService;

    @BeforeEach
    void setUp() {
        proveedorService = new ProveedorService(
                proveedorRepository,
                new ProveedorMapper(),
                proveedorPendienteService,
                productoRepository);
    }

    @Test
    void listarSoloDevuelveActivosComerciales() {
        final Proveedor proveedor = proveedor(10L, "Distribuidora Norte", true, false);
        when(proveedorRepository.findAllByActivoTrueAndSistemaFalseOrderByNombreAsc())
                .thenReturn(List.of(proveedor));

        final List<ProveedorResponseDTO> resultado = proveedorService.getAllProveedores();

        assertEquals(1, resultado.size());
        assertEquals("Distribuidora Norte", resultado.getFirst().nombre());
        verify(proveedorRepository).findAllByActivoTrueAndSistemaFalseOrderByNombreAsc();
    }

    @Test
    void buscarNormalizaTerminoYMapeaPagina() {
        final PageRequest pageable = PageRequest.of(0, 20);
        final Proveedor proveedor = proveedor(10L, "Distribuidora Norte", false, false);
        when(proveedorRepository.buscar("%norte%", false, pageable))
                .thenReturn(new PageImpl<>(List.of(proveedor), pageable, 1));

        final Page<ProveedorResponseDTO> resultado = proveedorService.buscar("  Norte  ", false, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertFalse(resultado.getContent().getFirst().activo());
    }

    @Test
    void crearRechazaNombrePendienteReservado() {
        final ProveedorRequestDTO request = request(" pendiente ");

        final ConflictException error = assertThrows(
                ConflictException.class,
                () -> proveedorService.createProveedor(request));

        assertEquals("El nombre PENDIENTE está reservado para uso interno del sistema", error.getMessage());
        verifyNoInteractions(proveedorRepository, productoRepository, proveedorPendienteService);
    }

    @Test
    void crearRechazaNombreDuplicadoSinDistinguirMayusculas() {
        final ProveedorRequestDTO request = request("Distribuidora Norte");
        when(proveedorRepository.existsByNombreIgnoreCase("Distribuidora Norte")).thenReturn(true);

        assertThrows(ConflictException.class, () -> proveedorService.createProveedor(request));

        verify(proveedorRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void actualizarRechazaModificarProveedorDelSistema() {
        final Proveedor pendiente = proveedor(1L, "PENDIENTE", true, true);
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(pendiente));

        assertThrows(ConflictException.class,
                () -> proveedorService.updateProveedor(1L, request("Otro")));

        verify(proveedorRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void desactivarReasignaProductosAntesDeMarcarInactivo() {
        final Proveedor proveedor = proveedor(10L, "Distribuidora Norte", true, false);
        final Proveedor pendiente = proveedor(1L, "PENDIENTE", true, true);
        when(proveedorRepository.findById(10L)).thenReturn(Optional.of(proveedor));
        when(proveedorPendienteService.obtener()).thenReturn(pendiente);
        when(productoRepository.reasignarProveedor(10L, pendiente)).thenReturn(3);

        proveedorService.deleteProveedor(10L);

        verify(productoRepository).reasignarProveedor(10L, pendiente);
        verify(proveedorRepository).save(proveedor);
        assertFalse(proveedor.isActivo());
    }

    @Test
    void desactivarNoMarcaInactivoSiFallaReasignacion() {
        final Proveedor proveedor = proveedor(10L, "Distribuidora Norte", true, false);
        final Proveedor pendiente = proveedor(1L, "PENDIENTE", true, true);
        when(proveedorRepository.findById(10L)).thenReturn(Optional.of(proveedor));
        when(proveedorPendienteService.obtener()).thenReturn(pendiente);
        when(productoRepository.reasignarProveedor(10L, pendiente))
                .thenThrow(new IllegalStateException("fallo simulado"));

        assertThrows(IllegalStateException.class, () -> proveedorService.deleteProveedor(10L));

        assertTrue(proveedor.isActivo());
        verify(proveedorRepository, never()).save(proveedor);
    }

    @Test
    void consultarProveedorInexistenteDevuelveErrorDeDominio() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> proveedorService.getProveedorById(99L));
    }

    private Proveedor proveedor(final Long id, final String nombre, final boolean activo, final boolean sistema) {
        final Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setNombre(nombre);
        proveedor.setPorcentajeComision(BigDecimal.ZERO);
        proveedor.setActivo(activo);
        proveedor.setSistema(sistema);
        return proveedor;
    }

    private ProveedorRequestDTO request(final String nombre) {
        return new ProveedorRequestDTO(nombre, null, null, null, null, BigDecimal.ZERO);
    }
}
