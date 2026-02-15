package com.kristianconk.api_papeleria.inventario;

public record InventarioMovimientoRequestDTO(
        Long productoId,
        Integer cantidad,
        String motivo,
        Double costoUnitario
) {
}
