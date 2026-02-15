package com.kristianconk.api_papeleria.producto;

public record ProductoRequestDTO(
        String codigoBarras,
        String nombre,
        String descripcion,
        Long categoriaId,
        Long proveedorId,
        Double costoCompra,
        Integer stockInicial,
        String unidadMedida
) {
    public ProductoRequestDTO {
        if (costoCompra == null || costoCompra <= 0) {
            throw new IllegalArgumentException("El costo de compra debe ser mayor a 0");
        }
    }
}
