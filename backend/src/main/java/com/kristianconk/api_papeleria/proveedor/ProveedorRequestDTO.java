package com.kristianconk.api_papeleria.proveedor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProveedorRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String nombre,

        @Size(max = 20, message = "El RFC no puede exceder 20 caracteres")
        @Pattern(
                regexp = "^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$",
                message = "El RFC no tiene un formato válido"
        )
        String rfc,

        @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
        @Pattern(
                regexp = "^[0-9+(). -]{7,50}$",
                message = "El teléfono contiene caracteres no permitidos"
        )
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede exceder 255 caracteres")
        String email,

        @Size(max = 255, message = "El contacto no puede exceder 255 caracteres")
        String contacto,
        @DecimalMin(value = "0.00", message = "La comisión no puede ser negativa")
        @DecimalMax(value = "100.00", message = "La comisión no puede superar 100%")
        @Digits(integer = 3, fraction = 2, message = "La comisión admite máximo 2 decimales")
        BigDecimal porcentajeComision
) {
    public ProveedorRequestDTO {
        nombre = normalizarTexto(nombre);
        rfc = normalizarTexto(rfc);
        telefono = normalizarTexto(telefono);
        email = normalizarTexto(email);
        contacto = normalizarTexto(contacto);
        if (rfc != null) rfc = rfc.toUpperCase();
        if (porcentajeComision == null) {
            porcentajeComision = BigDecimal.ZERO;
        }
    }

    private static String normalizarTexto(final String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
