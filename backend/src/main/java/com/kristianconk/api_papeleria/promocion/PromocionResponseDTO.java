package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.enums.TipoPromocion;

import java.time.LocalDateTime;

public record PromocionResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        TipoPromocion tipo,
        boolean activa,
        Long productoId,
        String productoNombre,
        Long categoriaId,
        String categoriaNombre,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Integer prioridad,
        ReglaDescuentoPorCantidadDTO reglaDescuentoPorCantidad,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
