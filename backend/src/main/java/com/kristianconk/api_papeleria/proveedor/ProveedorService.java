package com.kristianconk.api_papeleria.proveedor;

import com.kristianconk.api_papeleria.error.ConflictException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kristianconk.api_papeleria.proveedor.ProveedorPendienteService.NOMBRE_PROVEEDOR_PENDIENTE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProveedorService {
    
    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;
    private final ProveedorPendienteService proveedorPendienteService;
    private final ProductoRepository productoRepository;
    
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> getAllProveedores() {
        return proveedorRepository.findAllByActivoTrueAndSistemaFalseOrderByNombreAsc().stream()
                .map(proveedorMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProveedorResponseDTO> buscar(
            final String termino,
            final Boolean activo,
            final Pageable pageable) {
        final String patron = (termino == null || termino.isBlank())
                ? null
                : "%" + termino.trim().toLowerCase() + "%";
        return proveedorRepository.buscar(patron, activo, pageable)
                .map(proveedorMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public ProveedorResponseDTO getProveedorById(Long id) {
        return proveedorRepository.findById(id)
                .map(proveedorMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public long contarProductosAsignados(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + id);
        }
        return productoRepository.countByProveedorId(id);
    }

    
    @Transactional
    public ProveedorResponseDTO createProveedor(ProveedorRequestDTO request) {
        validarNombreDisponible(request.nombre(), null);
        log.info("[ProveedorService] - Creando proveedor con nombre: {}", request.nombre());
        Proveedor proveedor = proveedorMapper.toEntity(request);
        proveedor = proveedorRepository.save(proveedor);
        return proveedorMapper.toDto(proveedor);
    }
    
    @Transactional
    public ProveedorResponseDTO updateProveedor(Long id, ProveedorRequestDTO request) {
        log.info("[ProveedorService] - Actualizando proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
        if (proveedor.isSistema()) {
            throw new ConflictException("El proveedor PENDIENTE es una configuración del sistema y no se puede modificar");
        }
        validarNombreDisponible(request.nombre(), id);
        proveedorMapper.updateEntity(proveedor, request);
        proveedor = proveedorRepository.save(proveedor);
        return proveedorMapper.toDto(proveedor);
    }
    
    @Transactional
    public void deleteProveedor(Long id) {
        log.info("[ProveedorService] - Desactivando proveedor con ID: {}", id);
        final Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
        if (proveedor.isSistema()) {
            throw new ConflictException("El proveedor PENDIENTE es una configuración del sistema y no se puede eliminar");
        }
        final Proveedor proveedorPendiente = proveedorPendienteService.obtener();
        final int productosReasignados = productoRepository.reasignarProveedor(id, proveedorPendiente);
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        log.info("[ProveedorService] - Proveedor con ID: {} desactivado; productos reasignados: {}",
                id, productosReasignados);
    }

    private void validarNombreDisponible(final String nombre, final Long idActual) {
        if (NOMBRE_PROVEEDOR_PENDIENTE.equalsIgnoreCase(nombre)) {
            throw new ConflictException("El nombre PENDIENTE está reservado para uso interno del sistema");
        }
        final boolean duplicado = idActual == null
                ? proveedorRepository.existsByNombreIgnoreCase(nombre)
                : proveedorRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idActual);
        if (duplicado) {
            throw new ConflictException("Ya existe un proveedor con el nombre: " + nombre);
        }
    }
}
