package com.kristianconk.api_papeleria.proveedor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProveedorRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        String rfc,
        String telefono,
        String email,
        String contacto,
        @DecimalMin(value = "0.00", message = "La comisión no puede ser negativa")
        @DecimalMax(value = "100.00", message = "La comisión no puede superar 100%")
        @Digits(integer = 3, fraction = 2, message = "La comisión admite máximo 2 decimales")
        BigDecimal porcentajeComision
) {
    public ProveedorRequestDTO {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (porcentajeComision == null) {
            porcentajeComision = BigDecimal.ZERO;
        }
        if (porcentajeComision.compareTo(BigDecimal.ZERO) < 0
                || porcentajeComision.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("El porcentaje de comisión debe estar entre 0 y 100");
        }
    }
}
