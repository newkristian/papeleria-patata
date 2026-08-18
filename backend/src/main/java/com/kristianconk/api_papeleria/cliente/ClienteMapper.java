package com.kristianconk.api_papeleria.cliente;

import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    
    public ClienteResponseDTO toDto(Cliente cliente) {
        if (cliente == null) return null;
        if (cliente.getId() != null && cliente.getId() == 1L) {
            return ClienteResponseDTO.anonimo();
        }
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getTotalCompras(),
                cliente.getNivel()
        );
    }
    
    public Cliente toEntity(ClienteRequestDTO request) {
        if (request == null) return null;
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre());
        cliente.setTelefono(request.telefono());
        cliente.setEmail(request.email());
        cliente.setFechaRegistro(java.time.LocalDate.now());
        cliente.setTotalCompras(0.0);
        cliente.setNivel("Regular");
        return cliente;
    }
    
    public void updateEntity(Cliente cliente, ClienteRequestDTO request) {
        if (request.nombre() != null && !request.nombre().isBlank()) {
            cliente.setNombre(request.nombre());
        }
        if (request.telefono() != null && !request.telefono().isBlank()) {
            cliente.setTelefono(request.telefono());
        }
        if (request.email() != null) {
            cliente.setEmail(request.email());
        }
    }
}
