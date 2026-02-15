package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.ClienteResponseDTO;
import com.kristianconk.api_papeleria.enums.EstadoVenta;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.tienda.TiendaResponseDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDTO(
        Long id,
        String folio,
        UsuarioResponseDTO usuario,
        TiendaResponseDTO tienda,
        ClienteResponseDTO cliente,
        boolean ventaAnonima,
        LocalDateTime fechaVenta,
        Double subtotal,
        Double descuento,
        Double total,
        MetodoPago metodoPago,
        EstadoVenta estado,
        List<DetalleVentaResponseDTO> detalles
) {
}
