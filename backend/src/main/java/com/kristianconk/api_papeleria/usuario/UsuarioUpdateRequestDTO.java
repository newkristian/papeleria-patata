package com.kristianconk.api_papeleria.usuario;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequestDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
        String apellidos,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El email debe ser una dirección válida")
        @Size(max = 255, message = "El email no puede exceder 255 caracteres")
        String email,

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol,

        Long tiendaId
) {
}
