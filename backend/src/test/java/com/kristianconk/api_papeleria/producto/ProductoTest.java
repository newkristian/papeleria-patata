package com.kristianconk.api_papeleria.producto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoTest {

    @Test
    void calcularPrecioVenta_redondeaImporteConHalfUpADosDecimales() {
        final Producto producto = new Producto();
        producto.setCostoCompra(new BigDecimal("19.99"));
        producto.setPorcentajeGanancia(new BigDecimal("50.00"));

        producto.calcularPrecioVenta();

        assertEquals(new BigDecimal("29.99"), producto.getPrecioVenta());
    }
}
