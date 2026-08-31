package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.enums.TipoPromocion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PromocionRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String descripcion,

        @NotNull(message = "El tipo de promoción es obligatorio")
        TipoPromocion tipo,

        boolean activa,

        Long productoId,

        Long categoriaId,

        LocalDateTime fechaInicio,

        LocalDateTime fechaFin,

        @NotNull(message = "La prioridad es obligatoria")
        @PositiveOrZero(message = "La prioridad no puede ser negativa")
        Integer prioridad,

        @Valid
        ReglaDescuentoPorCantidadDTO reglaDescuentoPorCantidad
) {
    public PromocionRequestDTO {
        final boolean tieneProducto = productoId != null;
        final boolean tieneCategoria = categoriaId != null;
        if (tieneProducto == tieneCategoria) {
            throw new IllegalArgumentException(
                    "La promoción debe aplicar exactamente a un producto o a una categoría, no a ambos ni a ninguno");
        }

        if (fechaInicio != null && fechaFin != null && !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        if (tipo == TipoPromocion.DESCUENTO_POR_CANTIDAD && reglaDescuentoPorCantidad == null) {
            throw new IllegalArgumentException(
                    "La regla de descuento por cantidad es obligatoria para el tipo DESCUENTO_POR_CANTIDAD");
        }
    }
}
