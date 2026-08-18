package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.CategoriaDTO;
import com.kristianconk.api_papeleria.producto.foto.ProductoFotoDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorDTO;

import java.time.LocalDateTime;
import java.util.List;

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
        boolean cantidadDesconocida,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<ProductoFotoDTO> fotos,           // Lista completa de fotos
        ProductoFotoDTO fotoPrincipal           // Foto principal para acceso rápido
) {}
