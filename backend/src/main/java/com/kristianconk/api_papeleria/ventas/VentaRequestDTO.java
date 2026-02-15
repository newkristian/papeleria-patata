package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.enums.MetodoPago;

import java.util.List;

public record VentaRequestDTO(
        Long clienteId, // Opcional, null para venta anónima
        Double descuento,
        MetodoPago metodoPago,
        List<DetalleVentaRequestDTO> detalles
) {
    // Validación personalizada si es necesaria
    public VentaRequestDTO {
        if (descuento != null && (descuento < 0 || descuento > 100)) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }
    }
}
