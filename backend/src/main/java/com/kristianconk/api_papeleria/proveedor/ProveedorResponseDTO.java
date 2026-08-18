package com.kristianconk.api_papeleria.proveedor;

public record ProveedorResponseDTO(
        Long id,
        String nombre,
        String rfc,
        String telefono,
        String email,
        String contacto,
        Double porcentajeComision
) {
}
