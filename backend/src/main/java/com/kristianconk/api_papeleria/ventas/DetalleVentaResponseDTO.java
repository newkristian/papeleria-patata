package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.producto.ProductoResponseDTO;

import java.math.BigDecimal;

public record DetalleVentaResponseDTO(
        Long id,
        ProductoResponseDTO producto,
        Integer cantidad,
        BigDecimal precioListaUnitario,
        TipoDescuento tipoDescuento,
        BigDecimal porcentajeDescuento,
        BigDecimal montoDescuento,
        BigDecimal precioUnitarioFinal,
        BigDecimal subtotal,
        Long autorizadoPorUsuarioId,
        String motivoDescuento
) {
}
