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
        List<DetalleVentaRequestDTO> detalles,

        // Identificador de carrito/operación generado por el frontend. Obligatorio
        // únicamente cuando algún detalle trae una autorización de descuento manual
        // (T6), para que esa autorización quede atada a esta venta y no pueda
        // reutilizarse en otra.
        String carritoId
) {
    public VentaRequestDTO {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }
        final boolean requiereCarrito = detalles.stream()
                .anyMatch(detalle -> detalle.autorizacionDescuento() != null && !detalle.autorizacionDescuento().isBlank());
        if (requiereCarrito && (carritoId == null || carritoId.isBlank())) {
            throw new IllegalArgumentException(
                    "El identificador del carrito es requerido cuando se usa una autorización de descuento manual");
        }
    }

    public VentaRequestDTO(final Long clienteId, final MetodoPago metodoPago,
                            final List<DetalleVentaRequestDTO> detalles) {
        this(clienteId, metodoPago, detalles, null);
    }
}
