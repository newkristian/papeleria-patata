package com.kristianconk.api_papeleria.producto;

public record ProductoResponseDTO(
        Long id,
        String codigoBarras,
        String nombre,
        String descripcion,
        String categoria,
        String proveedor,
        Double precioVenta,
        Integer stockActual
) {}
