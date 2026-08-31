package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.categoria.CategoriaRepository;
import com.kristianconk.api_papeleria.enums.TipoPromocion;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
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
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> obtenerTodas() {
        log.info("[POS/PromocionService] - OBTENER_TODAS: buscando todas las promociones");
        return promocionRepository.findAll().stream()
                .map(PromocionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromocionResponseDTO obtenerPorId(final Long id) {
        log.info("[POS/PromocionService] - OBTENER_POR_ID: buscando promoción con ID: {}", id);
        return PromocionMapper.toDto(buscarPromocion(id));
    }

    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> obtenerPorProducto(final Long productoId) {
        log.info("[POS/PromocionService] - OBTENER_POR_PRODUCTO: buscando promociones del producto ID: {}",
                productoId);
        return promocionRepository.findByProductoId(productoId).stream()
                .map(PromocionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> obtenerPorCategoria(final Long categoriaId) {
        log.info("[POS/PromocionService] - OBTENER_POR_CATEGORIA: buscando promociones de la categoría ID: {}",
                categoriaId);
        return promocionRepository.findByCategoriaId(categoriaId).stream()
                .map(PromocionMapper::toDto)
                .collect(Collectors.toList());
    }

    public PromocionResponseDTO crear(final PromocionRequestDTO request) {
        log.info("[POS/PromocionService] - CREAR: creando promoción con nombre: {}", request.nombre());
        final Promocion promocion = PromocionMapper.toEntity(request);
        aplicarAlcance(promocion, request);
        aplicarRegla(promocion, request);
        final Promocion guardada = promocionRepository.save(promocion);
        log.info("[POS/PromocionService] - CREAR: promoción creada con ID: {}", guardada.getId());
        return PromocionMapper.toDto(guardada);
    }

    public PromocionResponseDTO actualizar(final Long id, final PromocionRequestDTO request) {
        log.info("[POS/PromocionService] - ACTUALIZAR: actualizando promoción con ID: {}", id);
        final Promocion promocionExistente = buscarPromocion(id);
        PromocionMapper.updateEntity(promocionExistente, request);
        aplicarAlcance(promocionExistente, request);
        aplicarRegla(promocionExistente, request);
        final Promocion guardada = promocionRepository.save(promocionExistente);
        log.info("[POS/PromocionService] - ACTUALIZAR: promoción con ID: {} actualizada", id);
        return PromocionMapper.toDto(guardada);
    }

    public void eliminar(final Long id) {
        log.info("[POS/PromocionService] - ELIMINAR: eliminando promoción con ID: {}", id);
        final Promocion promocion = buscarPromocion(id);
        promocionRepository.delete(promocion);
        log.info("[POS/PromocionService] - ELIMINAR: promoción con ID: {} eliminada", id);
    }

    private Promocion buscarPromocion(final Long id) {
        if (id == null) {
            log.error("[POS/PromocionService] - BUSCAR_PROMOCION: ID de promoción es nulo");
            throw new IllegalArgumentException("El ID de la promoción no puede ser nulo");
        }
        return promocionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/PromocionService] - BUSCAR_PROMOCION: promoción con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La promoción con ID " + id + " no existe");
                });
    }

    private void aplicarAlcance(final Promocion promocion, final PromocionRequestDTO request) {
        if (request.productoId() != null) {
            final Producto producto = productoRepository.findById(request.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El producto con ID " + request.productoId() + " no existe"));
            promocion.setProducto(producto);
            promocion.setCategoria(null);
            return;
        }
        final Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La categoría con ID " + request.categoriaId() + " no existe"));
        promocion.setCategoria(categoria);
        promocion.setProducto(null);
    }

    private void aplicarRegla(final Promocion promocion, final PromocionRequestDTO request) {
        if (request.tipo() != TipoPromocion.DESCUENTO_POR_CANTIDAD) {
            promocion.setReglaDescuentoPorCantidad(null);
            return;
        }
        ReglaDescuentoPorCantidad regla = promocion.getReglaDescuentoPorCantidad();
        if (regla == null) {
            regla = new ReglaDescuentoPorCantidad();
            regla.setPromocion(promocion);
            promocion.setReglaDescuentoPorCantidad(regla);
        }
        regla.setCantidadMinima(request.reglaDescuentoPorCantidad().cantidadMinima());
        regla.setPorcentaje(request.reglaDescuentoPorCantidad().porcentaje());
    }
}
