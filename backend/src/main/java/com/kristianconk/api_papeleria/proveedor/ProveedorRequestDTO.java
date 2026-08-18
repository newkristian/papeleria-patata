package com.kristianconk.api_papeleria.proveedor;

public record ProveedorRequestDTO(
        String nombre,
        String rfc,
        String telefono,
        String email,
        String contacto,
        Double porcentajeComision
) {
    public ProveedorRequestDTO {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (porcentajeComision == null) {
            porcentajeComision = 0.0;
        }
    }
}
