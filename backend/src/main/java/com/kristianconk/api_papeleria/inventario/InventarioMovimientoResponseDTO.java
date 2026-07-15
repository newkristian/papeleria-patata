package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import java.time.LocalDateTime;

public record InventarioMovimientoResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        String productoCodigoBarras,
        Long usuarioId,
        String usuarioNombre,
        TipoMovimiento tipo,
        Integer cantidad,
        String motivo,
        Double costoUnitario,
        LocalDateTime fechaMovimiento
) {}
