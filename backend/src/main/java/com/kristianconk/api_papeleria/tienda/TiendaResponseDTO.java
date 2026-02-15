package com.kristianconk.api_papeleria.tienda;

public record TiendaResponseDTO(
        Long id,
        String nombre,
        String direccion,
        String telefono
) {
}
