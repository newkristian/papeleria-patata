package com.kristianconk.api_papeleria.inventario;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

// DTO para ajuste de inventario
public record AjusteInventarioDTO(
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        @Positive(message = "El nuevo costo de compra debe ser mayor a cero")
        @Digits(integer = 17, fraction = 2, message = "El nuevo costo admite máximo 2 decimales")
        BigDecimal nuevoCostoCompra, // Opcional, si cambia el costo

        Boolean esFijarStockAbsoluto // Opcional, si es true se establece el stock en la cantidad y se apaga cantidadDesconocida
) {}
