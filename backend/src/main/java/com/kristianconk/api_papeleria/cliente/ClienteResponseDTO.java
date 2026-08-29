package com.kristianconk.api_papeleria.cliente;

import java.math.BigDecimal;

public record ClienteResponseDTO(
        Long id,
        String nombre,
        String telefono,
        BigDecimal totalCompras,
        String nivel
) {
    // Constructor para cliente anónimo
    public static ClienteResponseDTO anonimo() {
        return new ClienteResponseDTO(
                1L,
                "PÚBLICO GENERAL",
                "ANÓNIMO",
                BigDecimal.ZERO,
                "ANÓNIMO"
        );
    }
}
