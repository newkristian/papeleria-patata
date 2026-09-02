package com.kristianconk.api_papeleria.autorizacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta de una autorización emitida. {@code referencia} es el token en claro y
 * solo se entrega esta vez; el backend únicamente conserva su hash. Los campos de
 * comparación con la promoción automática son informativos (T7 los usará para mostrar
 * la diferencia al autorizador) y no se persisten como aplicados.
 */
public record AutorizacionDescuentoResponseDTO(
        String referencia,
        LocalDateTime expiraEn,
        BigDecimal porcentaje,
        BigDecimal montoDescuentoEstimado,
        BigDecimal precioFinalEstimado,
        boolean promocionAutomaticaDisponible,
        BigDecimal montoPromocionAutomatica,
        BigDecimal diferenciaVsPromocionAutomatica
) {
}
