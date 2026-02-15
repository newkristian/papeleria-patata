package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.producto.ProductoResponseDTO;

public record DetalleVentaResponseDTO(
        Long id,
        ProductoResponseDTO producto,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {
}
