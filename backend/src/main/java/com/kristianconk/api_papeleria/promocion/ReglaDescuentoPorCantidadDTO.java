package com.kristianconk.api_papeleria.promocion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReglaDescuentoPorCantidadDTO(
        @NotNull(message = "La cantidad mínima es obligatoria")
        @Positive(message = "La cantidad mínima debe ser mayor a 0")
        Integer cantidadMinima,

        @NotNull(message = "El porcentaje es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El porcentaje debe ser mayor a 0")
        @DecimalMax(value = "100.0", inclusive = false, message = "El porcentaje debe ser menor a 100")
        BigDecimal porcentaje
) {
}
