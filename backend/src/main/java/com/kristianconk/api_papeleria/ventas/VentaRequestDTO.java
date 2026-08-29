package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.enums.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VentaRequestDTO(
        Long clienteId,

        @NotNull(message = "El método de pago es requerido")
        MetodoPago metodoPago,

        @NotNull(message = "La venta debe tener al menos un detalle")
        @NotEmpty(message = "La venta debe tener al menos un detalle")
        @Valid
        List<DetalleVentaRequestDTO> detalles
) {
    public VentaRequestDTO {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }
    }
}
