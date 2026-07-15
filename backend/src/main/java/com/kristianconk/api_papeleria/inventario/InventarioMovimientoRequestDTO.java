package com.kristianconk.api_papeleria.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventarioMovimientoRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        @NotNull(message = "El costo unitario es obligatorio")
        @Positive(message = "El costo unitario debe ser mayor a cero")
        Double costoUnitario
) {}
