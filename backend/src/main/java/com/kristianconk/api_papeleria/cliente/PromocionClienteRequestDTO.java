package com.kristianconk.api_papeleria.cliente;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocionClienteRequestDTO(
        Long clienteId,
        String descripcion,
        @DecimalMin(value = "0.01", message = "El descuento debe ser mayor a cero")
        @DecimalMax(value = "100.00", message = "El descuento no puede superar 100%")
        @Digits(integer = 3, fraction = 2, message = "El descuento admite máximo 2 decimales")
        BigDecimal porcentajeDescuento,

        @PositiveOrZero(message = "El monto mínimo no puede ser negativo")
        @Digits(integer = 17, fraction = 2, message = "El monto mínimo admite máximo 2 decimales")
        BigDecimal montoMinimoCompra,
        LocalDate fechaFin
) {
}
