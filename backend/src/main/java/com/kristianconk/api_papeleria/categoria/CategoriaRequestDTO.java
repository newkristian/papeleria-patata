package com.kristianconk.api_papeleria.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String descripcion
) {
    public CategoriaRequestDTO {
        nombre = nombre == null ? null : nombre.trim();
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
    }
}
