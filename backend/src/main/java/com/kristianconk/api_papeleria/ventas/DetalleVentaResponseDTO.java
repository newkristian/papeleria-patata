package com.kristianconk.api_papeleria.ventas;

public record DetalleVentaResponseDTO(
        Long id,
        ProductoResponseDTO producto,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {
}
