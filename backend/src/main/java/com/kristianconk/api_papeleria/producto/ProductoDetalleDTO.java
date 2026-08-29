package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.CategoriaDTO;
import com.kristianconk.api_papeleria.producto.foto.ProductoFotoDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorDTO;

import java.math.BigDecimal;
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
        BigDecimal costoCompra,
        BigDecimal porcentajeGanancia,
        BigDecimal precioVenta,
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
