package com.kristianconk.api_papeleria.cliente;

public record ClienteResponseDTO(
        Long id,
        String nombre,
        String telefono,
        Double totalCompras,
        String nivel
) {
    // Constructor para cliente anónimo
    public static ClienteResponseDTO anonimo() {
        return new ClienteResponseDTO(
                1L,
                "PÚBLICO GENERAL",
                "ANÓNIMO",
                0.0,
                "ANÓNIMO"
        );
    }
}
