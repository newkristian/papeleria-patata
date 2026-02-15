package com.kristianconk.api_papeleria.producto;

// DTO para búsqueda avanzada
public record ProductoBusquedaDTO(
        String termino,
        Long categoriaId,
        Long proveedorId,
        Boolean activo,
        Double precioMin,
        Double precioMax,
        Boolean soloStockBajo
) {
    public ProductoBusquedaDTO {
        // Valores por defecto
        if (activo == null) activo = true;
    }
}
