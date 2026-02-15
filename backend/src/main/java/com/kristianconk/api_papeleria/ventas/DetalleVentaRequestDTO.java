package com.kristianconk.api_papeleria.ventas;

public record DetalleVentaRequestDTO(
        Long productoId,
        Integer cantidad,
        Double precioUnitario // El que tenga el producto en ese momento
) {
    public DetalleVentaRequestDTO {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (precioUnitario == null || precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
    }
}
