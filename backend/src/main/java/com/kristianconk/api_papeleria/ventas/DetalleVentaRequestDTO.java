package com.kristianconk.api_papeleria.ventas;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetalleVentaRequestDTO(
        @NotNull(message = "El ID del producto es requerido")
        Long productoId,

        @NotNull(message = "La cantidad es requerida")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad
) {
    public DetalleVentaRequestDTO {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }
}
