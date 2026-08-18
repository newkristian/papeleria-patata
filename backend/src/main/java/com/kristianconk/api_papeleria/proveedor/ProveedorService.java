package com.kristianconk.api_papeleria.proveedor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProveedorService {
    
    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;
    
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> getAllProveedores() {
        return proveedorRepository.findAll().stream()
                .map(proveedorMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProveedorResponseDTO getProveedorById(Long id) {
        return proveedorRepository.findById(id)
                .map(proveedorMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }
    
    @Transactional
    public ProveedorResponseDTO createProveedor(ProveedorRequestDTO request) {
        log.info("[ProveedorService] - createProveedor: {}", request);
        Proveedor proveedor = proveedorMapper.toEntity(request);
        proveedor = proveedorRepository.save(proveedor);
        return proveedorMapper.toDto(proveedor);
    }
    
    @Transactional
    public ProveedorResponseDTO updateProveedor(Long id, ProveedorRequestDTO request) {
        log.info("[ProveedorService] - updateProveedor: id={}, request={}", id, request);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        proveedorMapper.updateEntity(proveedor, request);
        proveedor = proveedorRepository.save(proveedor);
        return proveedorMapper.toDto(proveedor);
    }
    
    @Transactional
    public void deleteProveedor(Long id) {
        log.info("[ProveedorService] - deleteProveedor: id={}", id);
        if (!proveedorRepository.existsById(id)) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }
}
