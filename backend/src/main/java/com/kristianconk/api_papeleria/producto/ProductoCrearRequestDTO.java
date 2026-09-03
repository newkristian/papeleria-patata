package com.kristianconk.api_papeleria.producto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Locale;

public record ProductoCrearRequestDTO(
        @NotBlank(message = "El código de barras es obligatorio")
        @Size(max = 50, message = "El código de barras no puede exceder 50 caracteres")
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "El código de barras contiene caracteres no permitidos"
        )
        String codigoBarras,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "La categoría debe ser válida")
        Long categoriaId,

        @Positive(message = "El proveedor debe ser válido")
        Long proveedorId,

        @NotNull(message = "El costo de compra es obligatorio")
        @Positive(message = "El costo de compra debe ser mayor a 0")
        @Digits(integer = 17, fraction = 2, message = "El costo de compra admite máximo 2 decimales")
        BigDecimal costoCompra,

        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        @NotBlank(message = "La unidad de medida es obligatoria")
        @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
        String unidadMedida,

        @PositiveOrZero(message = "El porcentaje de ganancia debe ser positivo o cero")
        @DecimalMax(value = "999.99", message = "El porcentaje de ganancia no puede superar 999.99")
        @Digits(integer = 3, fraction = 2, message = "El porcentaje de ganancia admite máximo 2 decimales")
        BigDecimal porcentajeGananciaManual,

        Boolean cantidadDesconocida
) {
    public ProductoCrearRequestDTO {
        codigoBarras = normalizarCodigo(codigoBarras);
        nombre = normalizarRequerido(nombre);
        descripcion = normalizarOpcional(descripcion);
        unidadMedida = normalizarRequerido(unidadMedida);
        if (stockMinimo == null) stockMinimo = 5;
        if (cantidadDesconocida == null) cantidadDesconocida = true;
    }

    private static String normalizarRequerido(final String valor) {
        return valor == null ? null : valor.trim();
    }

    private static String normalizarCodigo(final String valor) {
        final String normalizado = normalizarRequerido(valor);
        return normalizado == null ? null : normalizado.toUpperCase(Locale.ROOT);
    }

    private static String normalizarOpcional(final String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
