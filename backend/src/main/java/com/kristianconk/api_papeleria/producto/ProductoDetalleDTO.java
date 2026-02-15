package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.CategoriaDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorDTO;

import java.time.LocalDateTime;

// Response DTO para detalle completo
public record ProductoDetalleDTO(
        Long id,
        String codigoBarras,
        String nombre,
        String descripcion,
        CategoriaDTO categoria,
        ProveedorDTO proveedor,
        Double costoCompra,
        Double porcentajeGanancia,
        Double precioVenta,
        Integer stockMinimo,
        Integer stockActual,
        String unidadMedida,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
