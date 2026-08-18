package com.kristianconk.api_papeleria.producto;

// Response DTO para listados (versión resumida)
public record ProductoListadoDTO(
        Long id,
        String codigoBarras,
        String nombre,
        String categoriaNombre,
        String proveedorNombre,
        Double precioVenta,
        Integer stockActual,
        Integer stockMinimo,
        boolean activo,
        boolean cantidadDesconocida,
        String urlThumbnail,                    // URL del thumbnail para listados
        boolean tieneFotos                       // Indicador útil para el frontend
) {}
