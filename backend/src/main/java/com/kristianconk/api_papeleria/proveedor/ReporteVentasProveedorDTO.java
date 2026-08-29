package com.kristianconk.api_papeleria.proveedor;

import com.kristianconk.api_papeleria.ventas.VentaPorProductoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteVentasProveedorDTO(
        Long proveedorId,
        String proveedorNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer totalVentas,
        BigDecimal montoTotalVentas,
        BigDecimal comisionTienda,
        BigDecimal montoAPagar,
        List<VentaPorProductoDTO> ventasPorProducto
) {
}
