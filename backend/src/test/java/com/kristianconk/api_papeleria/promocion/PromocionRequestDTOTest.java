package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.enums.TipoPromocion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromocionRequestDTOTest {

    private static final ReglaDescuentoPorCantidadDTO REGLA_VALIDA =
            new ReglaDescuentoPorCantidadDTO(10, new BigDecimal("5.00"));

    @Test
    void construir_productoYCategoriaAmbosNulos_lanzaIllegalArgumentException() {
        // Given / When / Then
        assertThrows(IllegalArgumentException.class, () -> new PromocionRequestDTO(
                "Descuento cuaderno", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                null, null, null, null, 0, REGLA_VALIDA));
    }

    @Test
    void construir_productoYCategoriaAmbosPresentes_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PromocionRequestDTO(
                "Descuento cuaderno", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                1L, 2L, null, null, 0, REGLA_VALIDA));
    }

    @Test
    void construir_fechaFinAnteriorAFechaInicio_lanzaIllegalArgumentException() {
        final LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 0, 0);
        final LocalDateTime fin = inicio.minusDays(1);

        assertThrows(IllegalArgumentException.class, () -> new PromocionRequestDTO(
                "Descuento cuaderno", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                1L, null, inicio, fin, 0, REGLA_VALIDA));
    }

    @Test
    void construir_tipoCantidadSinRegla_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PromocionRequestDTO(
                "Descuento cuaderno", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                1L, null, null, null, 0, null));
    }

    @Test
    void construir_datosValidos_noLanzaExcepcion() {
        assertDoesNotThrow(() -> new PromocionRequestDTO(
                "Descuento cuaderno", "desc", TipoPromocion.DESCUENTO_POR_CANTIDAD, true,
                1L, null, null, null, 0, REGLA_VALIDA));
    }
}
