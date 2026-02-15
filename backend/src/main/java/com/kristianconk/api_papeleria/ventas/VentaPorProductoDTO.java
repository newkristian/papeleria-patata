package com.kristianconk.api_papeleria.ventas;

public record VentaPorProductoDTO(
        Long productoId,
        String productoNombre,
        Integer cantidadVendida,
        Double montoTotal
) {
}
