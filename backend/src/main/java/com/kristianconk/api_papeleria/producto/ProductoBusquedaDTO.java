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
        termino = termino == null || termino.isBlank() ? null : termino.trim();
        if ((precioMin != null && precioMin.signum() < 0)
                || (precioMax != null && precioMax.signum() < 0)) {
            throw new IllegalArgumentException("Los filtros de precio no pueden ser negativos");
        }
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo");
        }
    }
}
