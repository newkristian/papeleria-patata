package com.kristianconk.api_papeleria.proveedor;

import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {
    
    public ProveedorResponseDTO toDto(Proveedor proveedor) {
        if (proveedor == null) return null;
        return new ProveedorResponseDTO(
                proveedor.getId(),
                proveedor.getNombre(),
                proveedor.getRfc(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getContacto(),
                proveedor.getPorcentajeComision()
        );
    }
    
    public Proveedor toEntity(ProveedorRequestDTO request) {
        if (request == null) return null;
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.nombre());
        proveedor.setRfc(request.rfc());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());
        proveedor.setContacto(request.contacto());
        proveedor.setPorcentajeComision(request.porcentajeComision());
        return proveedor;
    }
    
    public void updateEntity(Proveedor proveedor, ProveedorRequestDTO request) {
        if (request.nombre() != null && !request.nombre().isBlank()) {
            proveedor.setNombre(request.nombre());
        }
        if (request.rfc() != null) proveedor.setRfc(request.rfc());
        if (request.telefono() != null) proveedor.setTelefono(request.telefono());
        if (request.email() != null) proveedor.setEmail(request.email());
        if (request.contacto() != null) proveedor.setContacto(request.contacto());
        if (request.porcentajeComision() != null) {
            proveedor.setPorcentajeComision(request.porcentajeComision());
        }
    }
}
