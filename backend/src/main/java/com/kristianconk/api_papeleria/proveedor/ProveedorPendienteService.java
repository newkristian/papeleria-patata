package com.kristianconk.api_papeleria.proveedor;

import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProveedorPendienteService {

    public static final String NOMBRE_PROVEEDOR_PENDIENTE = "PENDIENTE";

    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public Proveedor obtener() {
        return proveedorRepository.findBySistemaTrue()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la configuración del proveedor pendiente"));
    }
}
