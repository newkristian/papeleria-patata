package com.kristianconk.api_papeleria.cliente;

public record ClienteRequestDTO(
        String nombre,
        String telefono,
        String email
) {
    public ClienteRequestDTO {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }
}
