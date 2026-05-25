package com.kristianconk.api_papeleria.tienda;

import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
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
public class TiendaService {

    private final TiendaRepository tiendaRepository;

    @Transactional(readOnly = true)
    public List<TiendaResponseDTO> obtenerTodas() {
        log.info("[POS/TiendaService] - OBTENER_TODAS: buscando todas las tiendas");
        final List<Tienda> tiendas = tiendaRepository.findAll();
        return tiendas.stream()
                .map(TiendaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TiendaResponseDTO obtenerPorId(final Long id) {
        log.info("[POS/TiendaService] - OBTENER_POR_ID: buscando tienda con ID: {}", id);
        if (id == null) {
            log.error("[POS/TiendaService] - OBTENER_POR_ID: ID de tienda es nulo");
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo");
        }
        final Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/TiendaService] - OBTENER_POR_ID: tienda con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La tienda con ID " + id + " no existe");
                });
        return TiendaMapper.toDto(tienda);
    }

    public TiendaResponseDTO crear(final TiendaRequestDTO request) {
        log.info("[POS/TiendaService] - CREAR: Creando nueva tienda con nombre: {}", request.nombre());
        final Tienda tienda = TiendaMapper.toEntity(request);
        final Tienda guardada = tiendaRepository.save(tienda);
        return TiendaMapper.toDto(guardada);
    }

    public TiendaResponseDTO actualizar(final Long id, final TiendaRequestDTO request) {
        log.info("[POS/TiendaService] - ACTUALIZAR: actualizando tienda con ID: {}", id);
        if (id == null) {
            log.error("[POS/TiendaService] - ACTUALIZAR: ID de tienda es nulo");
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo");
        }
        final Tienda tiendaExistente = tiendaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/TiendaService] - ACTUALIZAR: tienda con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La tienda con ID " + id + " no existe");
                });
        TiendaMapper.updateEntity(tiendaExistente, request);
        final Tienda guardada = tiendaRepository.save(tiendaExistente);
        return TiendaMapper.toDto(guardada);
    }

    public void eliminar(final Long id) {
        log.info("[POS/TiendaService] - ELIMINAR: eliminando tienda con ID: {}", id);
        if (id == null) {
            log.error("[POS/TiendaService] - ELIMINAR: ID de tienda es nulo");
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo");
        }
        final Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/TiendaService] - ELIMINAR: tienda con ID: {} no encontrada", id);
                    return new ResourceNotFoundException("La tienda con ID " + id + " no existe");
                });
        tiendaRepository.delete(tienda);
    }
}
