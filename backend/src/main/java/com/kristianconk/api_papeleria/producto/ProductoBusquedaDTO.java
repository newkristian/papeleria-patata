package com.kristianconk.api_papeleria.producto;

import java.math.BigDecimal;

// DTO para búsqueda avanzada
public record ProductoBusquedaDTO(
        String termino,
        Long categoriaId,
        Long proveedorId,
        Boolean activo,
        BigDecimal precioMin,
        BigDecimal precioMax,
        Boolean soloStockBajo
) {
    public ProductoBusquedaDTO {
        // Valores por defecto
        if (activo == null) activo = true;
    }
}
