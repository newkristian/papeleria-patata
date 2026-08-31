package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.enums.TipoDescuento;

import java.math.BigDecimal;

/**
 * Resultado explícito de evaluar promociones automáticas para un producto y una
 * cantidad consolidada. No representa un contrato HTTP: es el valor interno que
 * {@code VentaService} (T5) usará para fotografiar el detalle de venta.
 */
public record ResultadoPromocion(
        TipoDescuento tipo,
        Long promocionId,
        BigDecimal porcentaje,
        BigDecimal subtotalLista,
        BigDecimal montoDescuento,
        BigDecimal subtotalFinal
) {
    static ResultadoPromocion ninguna(final BigDecimal subtotalLista) {
        final BigDecimal cero = BigDecimal.ZERO.setScale(subtotalLista.scale());
        return new ResultadoPromocion(TipoDescuento.NINGUNO, null, cero, subtotalLista, cero, subtotalLista);
    }
}
