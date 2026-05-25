package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.enums.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VentaRequestDTO(
        Long clienteId,

        @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100")
        Double descuento,

        @NotNull(message = "El método de pago es requerido")
        MetodoPago metodoPago,

        @NotNull(message = "La venta debe tener al menos un detalle")
        @NotEmpty(message = "La venta debe tener al menos un detalle")
        @Valid
        List<DetalleVentaRequestDTO> detalles
) {
    public VentaRequestDTO {
        if (descuento == null) {
            descuento = 0.0;
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }
    }
}
