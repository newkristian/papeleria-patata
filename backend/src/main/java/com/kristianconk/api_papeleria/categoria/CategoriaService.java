package com.kristianconk.api_papeleria.categoria;

import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodas() {
        log.info("[POS/CategoriaService] - OBTENER_TODAS: buscando todas las categorías");
        final List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(CategoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(final Long id) {
        log.info("[POS/CategoriaService] - OBTENER_POR_ID: buscando categoría con ID: {}", id);
        if (id == null) {
            log.error("[POS/CategoriaService] - OBTENER_POR_ID: ID de categoría es nulo");
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
        final Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/CategoriaService] - OBTENER_POR_ID: categoría con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La categoría con ID " + id + " no existe");
                });
        return CategoriaMapper.toDto(categoria);
    }

    public CategoriaResponseDTO crear(final CategoriaRequestDTO request) {
        log.info("[POS/CategoriaService] - CREAR: creando nueva categoría con nombre: {}", request.nombre());
        final Categoria categoria = CategoriaMapper.toEntity(request);
        final Categoria guardada = categoriaRepository.save(categoria);
        log.info("[POS/CategoriaService] - CREAR: categoría creada con ID: {}", guardada.getId());
        return CategoriaMapper.toDto(guardada);
    }

    public CategoriaResponseDTO actualizar(final Long id, final CategoriaRequestDTO request) {
        log.info("[POS/CategoriaService] - ACTUALIZAR: actualizando categoría con ID: {}", id);
        if (id == null) {
            log.error("[POS/CategoriaService] - ACTUALIZAR: ID de categoría es nulo");
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
        final Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/CategoriaService] - ACTUALIZAR: categoría con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La categoría con ID " + id + " no existe");
                });
        CategoriaMapper.updateEntity(categoriaExistente, request);
        final Categoria guardada = categoriaRepository.save(categoriaExistente);
        log.info("[POS/CategoriaService] - ACTUALIZAR: categoría con ID: {} actualizada", id);
        return CategoriaMapper.toDto(guardada);
    }

    public void eliminar(final Long id) {
        log.info("[POS/CategoriaService] - ELIMINAR: eliminando categoría con ID: {}", id);
        if (id == null) {
            log.error("[POS/CategoriaService] - ELIMINAR: ID de categoría es nulo");
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
        final Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/CategoriaService] - ELIMINAR: categoría con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La categoría con ID " + id + " no existe");
                });
        final long cantidadProductos = productoRepository.countByCategoriaId(id);
        if (cantidadProductos > 0) {
            log.error("[POS/CategoriaService] - ELIMINAR: la categoría con ID: {} tiene {} productos asociados",
                    id, cantidadProductos);
            throw new IllegalStateException(
                    "No se puede eliminar la categoría porque tiene " + cantidadProductos
                            + " producto(s) asociado(s). Reasigne o elimine los productos primero.");
        }
        categoriaRepository.delete(categoria);
        log.info("[POS/CategoriaService] - ELIMINAR: categoría con ID: {} eliminada", id);
    }
}
