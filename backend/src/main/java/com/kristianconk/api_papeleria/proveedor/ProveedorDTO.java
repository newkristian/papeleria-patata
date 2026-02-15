package com.kristianconk.api_papeleria.proveedor;

public record ProveedorDTO(
        Long id,
        String nombre,
        String rfc,
        String contacto
) {
}
