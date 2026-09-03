package com.kristianconk.api_papeleria.categoria;

import com.kristianconk.api_papeleria.error.ConflictException;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.promocion.PromocionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PromocionRepository promocionRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, productoRepository, promocionRepository);
    }

    @Test
    void crearNormalizaCamposYRechazaNombreDuplicado() {
        final CategoriaRequestDTO request = new CategoriaRequestDTO("  Escolar  ", "  Útiles escolares  ");
        when(categoriaRepository.existsByNombreIgnoreCase("Escolar")).thenReturn(true);

        final ConflictException error = assertThrows(ConflictException.class, () -> categoriaService.crear(request));

        assertEquals("Ya existe una categoría con el nombre: Escolar", error.getMessage());
        verify(categoriaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void actualizarRechazaNombreUsadoPorOtraCategoria() {
        final Categoria categoria = categoria(5L, "Escolar");
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Oficina", 5L)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> categoriaService.actualizar(5L, new CategoriaRequestDTO("Oficina", null)));

        verify(categoriaRepository, never()).save(categoria);
    }

    @Test
    void eliminarRechazaCategoriaConProductos() {
        final Categoria categoria = categoria(5L, "Escolar");
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));
        when(productoRepository.countByCategoriaId(5L)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> categoriaService.eliminar(5L));

        verify(promocionRepository, never()).countByCategoriaId(5L);
        verify(categoriaRepository, never()).delete(categoria);
    }

    @Test
    void eliminarRechazaCategoriaConPromociones() {
        final Categoria categoria = categoria(5L, "Escolar");
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));
        when(productoRepository.countByCategoriaId(5L)).thenReturn(0L);
        when(promocionRepository.countByCategoriaId(5L)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> categoriaService.eliminar(5L));

        verify(categoriaRepository, never()).delete(categoria);
    }

    @Test
    void eliminarCategoriaSinRelaciones() {
        final Categoria categoria = categoria(5L, "Escolar");
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));

        categoriaService.eliminar(5L);

        verify(productoRepository).countByCategoriaId(5L);
        verify(promocionRepository).countByCategoriaId(5L);
        verify(categoriaRepository).delete(categoria);
    }

    private Categoria categoria(final Long id, final String nombre) {
        final Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }
}
