package com.kristianconk.api_papeleria.inventario;

public class InventarioMovimientoMapper {

    private InventarioMovimientoMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static InventarioMovimientoResponseDTO toDto(
            final InventarioMovimiento movimiento,
            final boolean ocultarCosto) {
        if (movimiento == null) {
            return null;
        }
        return new InventarioMovimientoResponseDTO(
                movimiento.getId(),
                movimiento.getProducto() != null ? movimiento.getProducto().getId() : null,
                movimiento.getProducto() != null ? movimiento.getProducto().getNombre() : null,
                movimiento.getProducto() != null ? movimiento.getProducto().getCodigoBarras() : null,
                movimiento.getUsuario() != null ? movimiento.getUsuario().getId() : null,
                movimiento.getUsuario() != null ? movimiento.getUsuario().getNombre() : null,
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getMotivo(),
                ocultarCosto ? null : movimiento.getCostoUnitario(),
                movimiento.getFechaMovimiento()
        );
    }
}
