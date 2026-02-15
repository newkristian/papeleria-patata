package com.kristianconk.api_papeleria.cliente;

import java.time.LocalDate;

public record PromocionClienteRequestDTO(
        Long clienteId,
        String descripcion,
        Double porcentajeDescuento,
        Double montoMinimoCompra,
        LocalDate fechaFin
) {
}
