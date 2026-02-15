package com.kristianconk.api_papeleria.producto;

import jakarta.validation.constraints.*;

public record ProductoRequestDTO(
        @NotBlank(message = "El código de barras es obligatorio")
        @Size(max = 50, message = "El código de barras no puede exceder 50 caracteres")
        String codigoBarras,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "El proveedor es obligatorio")
        Long proveedorId,

        @NotNull(message = "El costo de compra es obligatorio")
        @Positive(message = "El costo de compra debe ser mayor a 0")
        Double costoCompra,

        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        @NotBlank(message = "La unidad de medida es obligatoria")
        String unidadMedida,

        @PositiveOrZero(message = "El porcentaje de ganancia debe ser positivo o cero")
        Double porcentajeGananciaManual, // Opcional, si se quiere forzar un porcentaje

        Boolean activo
) {
    public ProductoRequestDTO {
        // Validaciones adicionales
        if (stockMinimo == null) stockMinimo = 5;
        if (activo == null) activo = true;
    }
}
