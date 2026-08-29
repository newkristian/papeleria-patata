package com.kristianconk.api_papeleria.producto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String codigoBarras,
        String nombre,
        String descripcion,
        String categoria,
        String proveedor,
        BigDecimal precioVenta,
        Integer stockActual
) {}
