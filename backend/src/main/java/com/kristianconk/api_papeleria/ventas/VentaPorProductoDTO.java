package com.kristianconk.api_papeleria.ventas;

import java.math.BigDecimal;

public record VentaPorProductoDTO(
        Long productoId,
        String productoNombre,
        Integer cantidadVendida,
        BigDecimal montoTotal
) {
}
