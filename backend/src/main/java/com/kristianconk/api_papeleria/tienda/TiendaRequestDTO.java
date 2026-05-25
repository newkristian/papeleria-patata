package com.kristianconk.api_papeleria.tienda;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TiendaRequestDTO(
                @NotBlank(message = "El nombre es obligatorio") @Size(max = 255, message = "El nombre no puede exceder 255 caracteres") String nombre,

                @Size(max = 255, message = "La dirección no puede exceder 255 caracteres") String direccion,

                @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres") String telefono,

                @Email(message = "El formato de correo electrónico es inválido") @Size(max = 255, message = "El correo electrónico no puede exceder 255 caracteres") String email) {
}
