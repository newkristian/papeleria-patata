package com.kristianconk.api_papeleria.proveedor;

import com.kristianconk.api_papeleria.ventas.VentaPorProductoDTO;

import java.time.LocalDate;
import java.util.List;

public record ReporteVentasProveedorDTO(
        Long proveedorId,
        String proveedorNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer totalVentas,
        Double montoTotalVentas,
        Double comisionTienda,
        Double montoAPagar,
        List<VentaPorProductoDTO> ventasPorProducto
) {
}
