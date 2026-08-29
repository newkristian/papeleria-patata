package com.kristianconk.api_papeleria.producto;

import java.math.BigDecimal;

// Response DTO para listados (versión resumida)
public record ProductoListadoDTO(
        Long id,
        String codigoBarras,
        String nombre,
        String categoriaNombre,
        String proveedorNombre,
        BigDecimal precioVenta,
        Integer stockActual,
        Integer stockMinimo,
        boolean activo,
        boolean cantidadDesconocida,
        String urlThumbnail,                    // URL del thumbnail para listados
        boolean tieneFotos                       // Indicador útil para el frontend
) {}
