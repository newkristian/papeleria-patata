package com.kristianconk.api_papeleria.ventas;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetalleVentaRequestDTO(
        @NotNull(message = "El ID del producto es requerido")
        Long productoId,

        @NotNull(message = "La cantidad es requerida")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        // Referencia opaca de una autorización de descuento manual (T6), emitida por
        // POST /api/v1/autorizaciones-descuento. Opcional: sin ella, la línea se
        // evalúa contra el motor de promociones automáticas.
        String autorizacionDescuento
) {
    public DetalleVentaRequestDTO {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }

    public DetalleVentaRequestDTO(final Long productoId, final Integer cantidad) {
        this(productoId, cantidad, null);
    }
}
