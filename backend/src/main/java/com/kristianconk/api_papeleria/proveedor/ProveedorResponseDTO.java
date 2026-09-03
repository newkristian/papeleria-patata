package com.kristianconk.api_papeleria.proveedor;

import java.math.BigDecimal;

public record ProveedorResponseDTO(
        Long id,
        String nombre,
        String rfc,
        String telefono,
        String email,
        String contacto,
        BigDecimal porcentajeComision,
        boolean activo,
        boolean sistema
) {
}
