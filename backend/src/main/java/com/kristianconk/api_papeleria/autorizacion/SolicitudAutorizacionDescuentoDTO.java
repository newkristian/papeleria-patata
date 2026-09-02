package com.kristianconk.api_papeleria.autorizacion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Solicitud de reautenticación para un descuento manual excepcional (T6). Las
 * credenciales viajan solo en este request, exclusivamente por HTTPS, y nunca se
 * conservan en el backend más allá de la validación.
 */
public record SolicitudAutorizacionDescuentoDTO(
        @NotBlank(message = "El usuario autorizador es requerido")
        String username,

        @NotBlank(message = "La contraseña es requerida")
        String password,

        @NotNull(message = "El ID del producto es requerido")
        Long productoId,

        @NotNull(message = "La cantidad es requerida")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        @NotNull(message = "El porcentaje es requerido")
        @DecimalMin(value = "0.00", message = "El porcentaje no puede ser negativo")
        @DecimalMax(value = "30.00", message = "El porcentaje manual no puede superar 30%")
        BigDecimal porcentaje,

        @NotBlank(message = "El motivo del descuento es requerido")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
        String motivo,

        @NotBlank(message = "El identificador del carrito es requerido")
        @Size(max = 100, message = "El identificador del carrito no puede superar 100 caracteres")
        String carritoId
) {
    public SolicitudAutorizacionDescuentoDTO {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (porcentaje == null
                || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(new BigDecimal("30.00")) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 30");
        }
    }
}
