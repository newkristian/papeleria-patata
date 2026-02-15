package com.kristianconk.api_papeleria.inventario;

import jakarta.validation.constraints.*;

// DTO para ajuste de inventario
public record AjusteInventarioDTO(
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        Double nuevoCostoCompra // Opcional, si cambia el costo
) {}
