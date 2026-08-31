package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.categoria.CategoriaRepository;
import com.kristianconk.api_papeleria.enums.TipoPromocion;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private PromocionService promocionService;

    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Cuaderno profesional");

        categoria = new Categoria();
        categoria.setId(2L);
        categoria.setNombre("Papelería");
    }

    @Test
    void crear_productoNoExiste_lanzaResourceNotFoundExceptionYNoPersiste() {
        // Given
        final PromocionRequestDTO request = requestParaProducto(99L, 10, "5.00");
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ResourceNotFoundException.class, () -> promocionService.crear(request));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    void crear_categoriaNoExiste_lanzaResourceNotFoundExceptionYNoPersiste() {
        final PromocionRequestDTO request = requestParaCategoria(99L, 10, "5.00");
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promocionService.crear(request));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    void crear_promocionValidaDeProducto_persisteAlcanceYReglaCorrectos() {
        // Given
        final PromocionRequestDTO request = requestParaProducto(1L, 10, "5.00");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // When
        final PromocionResponseDTO respuesta = promocionService.crear(request);

        // Then
        final ArgumentCaptor<Promocion> captor = ArgumentCaptor.forClass(Promocion.class);
        verify(promocionRepository).save(captor.capture());
        final Promocion guardada = captor.getValue();

        assertEquals(producto, guardada.getProducto());
        assertNull(guardada.getCategoria());
        assertEquals(10, guardada.getReglaDescuentoPorCantidad().getCantidadMinima());
        assertEquals(new BigDecimal("5.00"), guardada.getReglaDescuentoPorCantidad().getPorcentaje());
        assertEquals(guardada, guardada.getReglaDescuentoPorCantidad().getPromocion());
        assertEquals(producto.getId(), respuesta.productoId());
    }

    @Test
    void crear_dosEscalonesParaElMismoProducto_persistenComoPromocionesIndependientes() {
        // Given: 10 unidades -> 5% y 20 unidades -> 8% sobre el mismo producto.
        final PromocionRequestDTO escalon1 = requestParaProducto(1L, 10, "5.00");
        final PromocionRequestDTO escalon2 = requestParaProducto(1L, 20, "8.00");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // When
        promocionService.crear(escalon1);
        promocionService.crear(escalon2);

        // Then
        final ArgumentCaptor<Promocion> captor = ArgumentCaptor.forClass(Promocion.class);
        verify(promocionRepository, times(2)).save(captor.capture());
        final List<Promocion> guardadas = captor.getAllValues();

        assertEquals(2, guardadas.size());
        assertEquals(10, guardadas.get(0).getReglaDescuentoPorCantidad().getCantidadMinima());
        assertEquals(new BigDecimal("5.00"), guardadas.get(0).getReglaDescuentoPorCantidad().getPorcentaje());
        assertEquals(20, guardadas.get(1).getReglaDescuentoPorCantidad().getCantidadMinima());
        assertEquals(new BigDecimal("8.00"), guardadas.get(1).getReglaDescuentoPorCantidad().getPorcentaje());
    }

    @Test
    void actualizar_promocionNoExiste_lanzaResourceNotFoundException() {
        final PromocionRequestDTO request = requestParaProducto(1L, 10, "5.00");
        when(promocionRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promocionService.actualizar(5L, request));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    void actualizar_promocionExistente_actualizaReglaEnLaMismaEntidad() {
        // Given
        final Promocion existente = new Promocion();
        existente.setId(7L);
        existente.setProducto(producto);
        existente.setTipo(TipoPromocion.DESCUENTO_POR_CANTIDAD);
        final ReglaDescuentoPorCantidad reglaExistente = new ReglaDescuentoPorCantidad();
        reglaExistente.setId(3L);
        reglaExistente.setPromocion(existente);
        reglaExistente.setCantidadMinima(10);
        reglaExistente.setPorcentaje(new BigDecimal("5.00"));
        existente.setReglaDescuentoPorCantidad(reglaExistente);

        final PromocionRequestDTO request = requestParaProducto(1L, 20, "8.00");
        when(promocionRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // When
        promocionService.actualizar(7L, request);

        // Then: se reutiliza la misma fila de regla (mismo ID), solo cambian sus valores.
        assertEquals(3L, existente.getReglaDescuentoPorCantidad().getId());
        assertEquals(20, existente.getReglaDescuentoPorCantidad().getCantidadMinima());
        assertEquals(new BigDecimal("8.00"), existente.getReglaDescuentoPorCantidad().getPorcentaje());
    }

    @Test
    void eliminar_promocionNoExiste_lanzaResourceNotFoundExceptionYNoElimina() {
        when(promocionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promocionService.eliminar(9L));
        verify(promocionRepository, never()).delete(any());
    }

    private PromocionRequestDTO requestParaProducto(final Long productoId, final int cantidadMinima,
                                                      final String porcentaje) {
        return new PromocionRequestDTO(
                "Descuento por cantidad", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                productoId, null, null, null, 0,
                new ReglaDescuentoPorCantidadDTO(cantidadMinima, new BigDecimal(porcentaje)));
    }

    private PromocionRequestDTO requestParaCategoria(final Long categoriaId, final int cantidadMinima,
                                                       final String porcentaje) {
        return new PromocionRequestDTO(
                "Descuento por cantidad", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                null, categoriaId, null, null, 0,
                new ReglaDescuentoPorCantidadDTO(cantidadMinima, new BigDecimal(porcentaje)));
    }
}
